/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.primitives.Take
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.mapper.mapToMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.io.zip.IFileReader
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.Project
import org.wycliffeassociates.resourcecontainer.entity.Source
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.regex.Pattern
import javax.inject.Inject

class ProjectImporter @Inject constructor(
    private val resourceContainerImporter: ImportResourceContainer,
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepository: IResourceMetadataRepository,
    private val collectionRepository: ICollectionRepository,
    private val contentRepository: IContentRepository,
    private val takeRepository: ITakeRepository,
    private val languageRepository: ILanguageRepository,
    private val resourceRepository: IResourceRepository
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    private val contentCache = mutableMapOf<ContentSignature, Content>()
    private val takeFilenamePattern = run {
        val chapter = """_c(\d+)"""
        val verse = """(?:_v(\d+))?"""
        val sort = """(?:_s(\d+))?"""
        val type = """(?:_([A-Za-z]+))?"""
        val take = """_t(\d+)"""
        val extensionDelim = """\."""
        Pattern.compile(chapter + verse + sort + type + take + extensionDelim)
    }

    fun isResumableProject(resourceContainer: File): Boolean {
        return try {
            hasInProgressMarker(resourceContainer)
        } catch (e: IOException) {
            false
        }
    }

    fun getSourceMetadata(resourceContainer: File): Maybe<ResourceMetadata> {
        return Maybe.fromCallable {
            val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
            val manifestSources = manifest.dublinCore.source.toSet()
            val manifestProject = manifest.projects.single()
            val sourceCollection = findSourceCollection(manifestSources, manifestProject)
            sourceCollection.resourceContainer
        }
    }

    fun importResumableProject(resourceContainer: File): Single<ImportResult> {
        return Single.fromCallable {
            try {
                val manifest: Manifest = ResourceContainer.load(resourceContainer).use { it.manifest }
                val manifestSources = manifest.dublinCore.source.toSet()
                val manifestProject = try {
                    manifest.projects.single()
                } catch (t: Throwable) {
                    log.error("In-progress import must have 1 project, but this has {}", manifest.projects.count())
                    throw ImportException(ImportResult.INVALID_RC)
                }

                directoryProvider.newFileReader(resourceContainer).use { fileReader ->
                    val existingSource = fetchExistingSource(manifestProject, manifestSources)
                    val sourceCollection = if (existingSource == null) {
                        importSources(fileReader)
                        findSourceCollection(manifestSources, manifestProject)
                    } else {
                        existingSource
                    }
                    syncProjectVersion(manifest, sourceCollection.resourceContainer!!.version)

                    val metadata = languageRepository
                        .getBySlug(manifest.dublinCore.language.identifier)
                        .map { language ->
                            manifest.dublinCore.mapToMetadata(resourceContainer, language)
                        }
                        .blockingGet()

                    importResumableProject(fileReader, metadata, manifestProject, sourceCollection)
                }

                ImportResult.SUCCESS
            } catch (e: ImportException) {
                e.result
            } catch (e: Exception) {
                log.error("Failed to import in-progress project", e)
                ImportResult.IMPORT_ERROR
            }
        }
    }

    private fun importResumableProject(
        fileReader: IFileReader,
        metadata: ResourceMetadata,
        manifestProject: Project,
        sourceCollection: Collection
    ) {
        val sourceMetadata = sourceCollection.resourceContainer!!
        val derivedProject = createDerivedProjects(metadata.language, sourceCollection)

        val translation = createTranslation(sourceMetadata.language, metadata.language)

        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            metadata,
            derivedProject
        )

        projectFilesAccessor.initializeResourceContainerInDir()
        projectFilesAccessor.copySourceFiles(fileReader)

        importContributorInfo(metadata, projectFilesAccessor)
        importTakes(
            fileReader,
            derivedProject,
            manifestProject,
            metadata,
            sourceCollection,
            projectFilesAccessor
        )

        translation.modifiedTs = LocalDateTime.now()
        languageRepository.updateTranslation(translation).subscribe()
    }

    private fun importContributorInfo(
        metadata: ResourceMetadata,
        projectFilesAccessor: ProjectFilesAccessor
    ) {
        val contributors = ResourceContainer.load(metadata.path).use { rc ->
            rc.manifest.dublinCore.contributor.map { Contributor(it) }
        }
        if (contributors.isNotEmpty()) {
            projectFilesAccessor.setContributorInfo(contributors)
        }
    }

    private fun importTakes(
        fileReader: IFileReader,
        project: Collection,
        manifestProject: Project,
        metadata: ResourceMetadata,
        sourceCollection: Collection,
        projectFilesAccessor: ProjectFilesAccessor
    ) {
        val collectionForTakes = when (metadata.type) {
            // Work around the quirk that resource takes are attached to source, not target project
            ContainerType.Help -> sourceCollection
            else -> project
        }
        val sourceMetadata = sourceCollection.resourceContainer!!

        val selectedTakes = fileReader
            .bufferedReader(RcConstants.SELECTED_TAKES_FILE)
            .useLines { it.toSet() }

        projectFilesAccessor.copySelectedTakesFile(fileReader)

        projectFilesAccessor.copyTakeFiles(fileReader, manifestProject)
            .doOnError { e ->
                log.error("Error in importTakes, project: $project, manifestProject: $manifestProject")
                log.error("metadata: $metadata, sourceMetadata: $sourceMetadata")
                log.error("sourceCollection: $sourceCollection", e)
            }
            .subscribe { newTakeFile ->
                insertTake(
                    newTakeFile,
                    projectFilesAccessor.audioDir,
                    collectionForTakes,
                    sourceMetadata,
                    selectedTakes
                )
            }
    }

    private fun insertTake(
        filepath: String,
        projectAudioDir: File,
        project: Collection,
        metadata: ResourceMetadata,
        selectedTakes: Set<String>
    ) {
        parseNumbers(filepath)?.let { (sig, takeNumber) ->
            getContent(sig, project, metadata)?.let { chunk ->
                val now = LocalDate.now()
                val file = File(filepath).canonicalFile
                val relativeFile = file.relativeTo(projectAudioDir.canonicalFile)

                val take = Take(file.name, file, takeNumber, now, null, false, listOf())
                take.id = takeRepository.insertForContent(take, chunk).blockingGet()

                if (relativeFile.invariantSeparatorsPath in selectedTakes) {
                    chunk.selectedTake = take
                    contentRepository.update(chunk).blockingAwait()
                }
            }
        }
    }

    private fun createDerivedProjects(language: Language, sourceCollection: Collection): Collection {
        return CreateProject(collectionRepository, resourceMetadataRepository)
            .create(sourceCollection, language)
            .blockingGet()
    }

    private fun findSourceCollection(manifestSources: Set<Source>, manifestProject: Project): Collection {
        val allSourceProjects = collectionRepository.getSourceProjects().blockingGet()
        val sourceCollection: Collection? = allSourceProjects
            .asSequence()
            .filter { sourceProject ->
                sourceProject.resourceContainer
                    ?.run { Source(identifier, language.slug, version) }
                    ?.let { it in manifestSources }
                    ?: false
            }
            .filter {
                it.slug == manifestProject.identifier
            }
            .firstOrNull()

        if (sourceCollection == null) {
            log.error("Failed to find source that matches requested import.")
            throw ImportException(ImportResult.IMPORT_ERROR)
        }
        return sourceCollection
    }

    private fun hasInProgressMarker(resourceContainer: File): Boolean {
        return directoryProvider.newFileReader(resourceContainer).use {
            it.exists(RcConstants.SELECTED_TAKES_FILE)
        }
    }

    /**
     * Find the relevant source (if any) for the project, regardless of version
     */
    private fun fetchExistingSource(
        manifestProject: Project,
        requestedSources: Set<Source>
    ): Collection? {
        return collectionRepository.getSourceProjects().blockingGet()
            .asSequence()
            .firstOrNull { collection ->
                requestedSources.any { source ->
                    manifestProject.identifier == collection.slug &&
                            source.identifier == collection.resourceContainer!!.identifier &&
                            source.language == collection.resourceContainer!!.language.slug
                }
            }
    }

    private fun importSources(fileReader: IFileReader) {
        val sourceFiles: Sequence<String> = fileReader
            .list(RcConstants.SOURCE_DIR)
            .filter {
                val ext = it.substringAfterLast(".")
                OratureFileFormat.isSupported(ext)
            }

        val firstTry: Map<String, ImportResult> = sourceFiles
            .map { importSource(it, fileReader) }
            .toMap()

        // If our first try results contain both an UNMATCHED_HELP and a SUCCESS, then a retry might help.
        if (firstTry.containsValue(ImportResult.SUCCESS)) {
            firstTry
                .filter { (_, result) -> result == ImportResult.UNMATCHED_HELP }
                .forEach { (file, _) -> importSource(file, fileReader) }
        }
    }

    private fun importSource(fileInZip: String, fileReader: IFileReader): Pair<String, ImportResult> {
        val name = File(fileInZip).nameWithoutExtension
        val result = resourceContainerImporter
            .import(name, fileReader.stream(fileInZip))
            .blockingGet()
        log.debug("Import source resource container {} result {}", name, result)
        return fileInZip to result
    }

    private fun createTranslation(sourceLanguage: Language, targetLanguage: Language): Translation {
        val translation = Translation(sourceLanguage, targetLanguage, LocalDateTime.now())
        val id = languageRepository
            .insertTranslation(translation)
            .doOnError { e ->
                log.error("Error in inserting translation", e)
            }
            .onErrorReturnItem(0)
            .blockingGet()
        translation.id = id
        return translation
    }

    private fun getContent(sig: ContentSignature, project: Collection, metadata: ResourceMetadata): Content? {
        return contentCache.computeIfAbsent(sig) { (chapter, verse, sort, type) ->
            val collection: Observable<Collection> = collectionRepository
                .getChildren(project)
                .flattenAsObservable { it }
                .filter { chapterCollection ->
                    chapterCollection.slug.endsWith("_$chapter")
                }

            val metaOrHelpStartVerse = when (type) {
                ContentType.META -> 1
                else -> 0
            }

            val isHelpVerse = metadata.type == ContainerType.Help && verse != null

            val content: Maybe<Content> = collection
                .flatMap {
                    contentRepository.getByCollection(it).flattenAsObservable { it }
                }
                // If we have help resource chunks, filter to linked TEXT chunks
                .filter { if (isHelpVerse) it.type == ContentType.TEXT else true }
                // If we have help resource chunks, fetch resources by linked TEXT chunk
                // and related resource container
                .flatMap { if (isHelpVerse) resourceRepository.getResources(it, metadata) else Observable.just(it) }
                // If type isn't specified in filename, match on TEXT.
                .filter { content -> content.type == (type ?: ContentType.TEXT) }
                // If verse number isn't specified in filename, assume chapter helps or meta.
                .filter { content -> content.start == (verse ?: metaOrHelpStartVerse) }
                // If sort isn't specified in filename,
                // DON'T filter on it, because we only need it for helps and meta.
                .filter { content -> sort?.let { content.sort == sort } ?: true }
                .firstElement()

            content.blockingGet()
        }
    }

    private fun parseNumbers(filename: String): TakeSignature? {
        val matcher = takeFilenamePattern.matcher(filename)
        return if (matcher.find()) {
            val chapter = matcher.group(1).toInt()
            val verse = matcher.group(2)?.toIntOrNull()
            val sort = matcher.group(3)?.toIntOrNull()
            val type = matcher.group(4)?.let { ContentType.of(it) }
            val take = matcher.group(5).toInt()
            TakeSignature(ContentSignature(chapter, verse, sort, type), take)
        } else {
            null
        }
    }

    /** Applies source version to target version before importing. */
    private fun syncProjectVersion(projectManifest: Manifest, version: String) {
        projectManifest.dublinCore.version = version
    }

    data class ContentSignature(val chapter: Int, val verse: Int?, val sort: Int?, val type: ContentType?)
    data class TakeSignature(val contentSignature: ContentSignature, val take: Int)
}
