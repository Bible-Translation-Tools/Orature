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

import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.DEFAULT_RC_SLUG
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.io.zip.IFileWriter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Media
import org.wycliffeassociates.resourcecontainer.entity.MediaManifest
import org.wycliffeassociates.resourcecontainer.entity.MediaProject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class ProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val workbookRepository: IWorkbookRepository
) {
    @Inject
    lateinit var audioExporter: AudioExporter

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor,
        fileFormat: OratureFileFormat = OratureFileFormat.ORATURE
    ): Single<ExportResult> {
        return Single
            .fromCallable {
                val projectSourceMetadata = workbook.source.linkedResources
                    .firstOrNull { it.identifier == projectMetadataToExport.identifier }
                    ?: workbook.source.resourceMetadata

                val projectToExportIsBook: Boolean =
                    projectMetadataToExport.identifier == workbook.target.resourceMetadata.identifier

                val contributors = projectFilesAccessor.getContributorInfo()
                val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
                val zipFile = directory.resolve(zipFilename)

                projectFilesAccessor.initializeResourceContainerInFile(workbook, zipFile)
                setContributorInfo(contributors, zipFile)

                directoryProvider.newFileWriter(zipFile).use { fileWriter ->
                    projectFilesAccessor.copyTakeFiles(
                        fileWriter,
                        workbook,
                        workbookRepository,
                        projectToExportIsBook
                    )

                    val linkedResource = workbook.source.linkedResources
                        .firstOrNull { it.identifier == projectMetadataToExport.identifier }
                    projectFilesAccessor.copySourceFiles(fileWriter, linkedResource)
                    projectFilesAccessor.writeSelectedTakesFile(fileWriter, workbook, projectToExportIsBook)
                }

                if (fileFormat != OratureFileFormat.ZIP) {
                    restoreFileExtension(zipFile, fileFormat.extension)
                }

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                log.error("Failed to export in-progress project", it)
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    fun exportAsSource(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val projectSourceMetadata = workbook.source.linkedResources
            .firstOrNull { it.identifier == projectMetadataToExport.identifier }
            ?: workbook.source.resourceMetadata

        val contributors = projectFilesAccessor.getContributorInfo()
        val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
        val zipFile = directory.resolve(zipFilename)

        projectFilesAccessor.initializeResourceContainerInFile(workbook, zipFile)
        setContributorInfo(contributors, zipFile)

        val fileWriter = directoryProvider.newFileWriter(zipFile)
        return copyTakesAsSource(workbook, fileWriter, contributors)
            .doOnComplete {
                // calling writer.close() multiple times causes an exception, thus,
                // it should not be invoked again in finally {} block
                fileWriter.close()
                updateManifest(zipFile, workbook.source.resourceMetadata.path, workbook)
            }
            .doOnError {
                fileWriter.close()
            }
            .doFinally {
                restoreFileExtension(zipFile, OratureFileFormat.ORATURE.extension)
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    fun exportMp3(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val isBook = projectMetadataToExport.identifier == workbook.target.resourceMetadata.identifier
        return if (isBook) {
            exportBookMp3(directory, workbook, projectFilesAccessor)
        } else {
            exportResourceMp3(directory, projectMetadataToExport, workbook, projectFilesAccessor)
        }
    }

    private fun exportBookMp3(
        directory: File,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val outputProjectDir = directory.resolve(workbook.target.slug).apply { mkdirs() }
        val contributors = projectFilesAccessor.getContributorInfo()
        val license = License.get(workbook.target.resourceMetadata.license)

        return workbook.target.chapters
            .filter { it.audio.selected.value?.value != null }
            .flatMapCompletable { chapter ->
                chapter.audio.selected.value!!.value!!.let {
                    val outputFile = outputProjectDir.resolve("chapter-${chapter.sort}.mp3")
                    audioExporter.exportMp3(it.file, outputFile, license, contributors)
                }
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun exportResourceMp3(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val contributors = projectFilesAccessor.getContributorInfo()
        val license = License.get(workbook.target.resourceMetadata.license)

        val outputProjectDir = directory
            .resolve("${workbook.target.slug}-${projectMetadataToExport.identifier}")
            .apply { mkdirs() }

        return projectFilesAccessor.selectedChapterFilePaths(workbook, false)
            .toObservable()
            .flatMapCompletable { takePath ->
                val takeFile = projectFilesAccessor.audioDir.resolve(takePath)
                val outputFile = outputProjectDir.resolve(
                    takePath
                ).let {
                    it.parentFile.mkdirs()
                    it.parentFile.resolve("${takeFile.nameWithoutExtension}.mp3")
                }
                audioExporter.exportMp3(takeFile, outputFile, license, contributors)
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun copyTakesAsSource(
        workbook: Workbook,
        fileWriter: IFileWriter,
        contributors: List<Contributor>
    ): Completable {
        val tempExportDir = directoryProvider.tempDirectory
            .resolve("export${Date().time}")
            .apply { mkdirs() }

        val license = License.get(workbook.target.resourceMetadata.license)

        return workbook.target.chapters
            .filter { it.audio.selected.value?.value != null }
            .flatMapCompletable { chapter ->
                chapter.audio.selected.value!!.value!!.let { take ->
                    val takeToExport = tempExportDir.resolve(
                        templateAudioFileName(
                            workbook.target.language.slug,
                            workbook.target.resourceMetadata.identifier,
                            workbook.target.slug,
                            chapter.sort.toString(),
                            AudioFileFormat.MP3.extension
                        )
                    )
                    take.file.copyTo(takeToExport)
                    // update cues for newly copied takes
                    val cues = AudioFile(take.file).metadata.getCues()
                    AudioFile(takeToExport).apply {
                        cues.forEach { metadata.addCue(it.location, it.label) }
                        update()
                    }
                    audioExporter.exportMp3(take.file, takeToExport, license, contributors)
                }
            }
            .andThen(
                Completable
                    .fromAction {
                        // include the .cue files associated with takes within the same directory
                        fileWriter.copyDirectory(tempExportDir, RcConstants.SOURCE_MEDIA_DIR)
                    }
                    .subscribeOn(Schedulers.io())
            )
    }

    private fun updateManifest(
        rcFile: File,
        sourceRCFile: File,
        workbook: Workbook
    ) {
        val languageSlug = workbook.target.language.slug
        val resourceSlug = workbook.target.resourceMetadata.identifier
        val projectSlug = workbook.target.slug

        ResourceContainer.load(rcFile).use { rc ->
            // update manifest.yaml
            ResourceContainer.load(sourceRCFile).use { sourceRC ->
                val projects = sourceRC.manifest.projects
                rc.manifest.projects = projects

                // copy source text
                sourceRC.accessor
                    .getInputStreams(".", "usfm").forEach { fileName, inputStream ->
                        inputStream.use { ins ->
                            rc.accessor.write(fileName) {
                                it.buffered().use { out ->
                                    out.write(ins.buffered().readBytes())
                                }
                            }
                        }
                    }
            }
            // update media (.yaml + files)
            val mediaManifest = let {
                MediaManifest().apply {
                    projects = listOf(MediaProject(identifier = projectSlug))
                }
            }
            var mediaUrl = templateAudioFileName(
                languageSlug, resourceSlug, projectSlug, "{chapter}", AudioFileFormat.MP3.extension
            )
            val mp3Media = Media(
                identifier = "mp3",
                version = "",
                url = "",
                quality = listOf(),
                chapterUrl = "${RcConstants.SOURCE_MEDIA_DIR}/${mediaUrl}"
            )

            mediaUrl = templateAudioFileName(
                languageSlug, resourceSlug, projectSlug, "{chapter}", "cue"
            )
            val cueMedia = Media(
                identifier = "cue",
                version = "",
                url = "",
                quality = listOf(),
                chapterUrl = "${RcConstants.SOURCE_MEDIA_DIR}/${mediaUrl}"
            )

            mediaManifest.projects.first().media = listOf(mp3Media, cueMedia)
            rc.media = mediaManifest

            rc.write()
        }
    }

    private fun makeExportFilename(workbook: Workbook, metadata: ResourceMetadata): String {
        val lang = workbook.target.language.slug
        val resource = if (workbook.source.language.slug == workbook.target.language.slug) {
            metadata.identifier
        } else {
            DEFAULT_RC_SLUG
        }
        val project = workbook.target.slug
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$resource-$project-$timestamp.zip"
    }

    private fun setContributorInfo(contributors: List<Contributor>, projectFile: File) {
        ResourceContainer.load(projectFile).use { rc ->
            rc.manifest.dublinCore.contributor = contributors.map { it.name }.toMutableList()
            rc.writeManifest()
        }
    }

    private fun templateAudioFileName(
        language: String,
        resource: String,
        project: String,
        chapterLabel: String,
        extension: String
    ): String {
        return "${language}_${resource}_${project}_c${chapterLabel}.$extension"
    }

    private fun restoreFileExtension(file: File, extension: String) {
        val fileName = file.nameWithoutExtension + ".$extension"
        // using nio Files.move() instead of file.rename() for platform independent
        Files.move(
            file.toPath(),
            file.parentFile.resolve(fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}
