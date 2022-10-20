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
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.ConcatenateAudio
import org.wycliffeassociates.otter.common.domain.content.FileNamer
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.utils.mapNotNull
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.inject.Inject

abstract class ProjectExporter {
    @Inject
    lateinit var concatenateAudio: ConcatenateAudio

    @Inject
    lateinit var takeActions: TakeActions

    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val compositeDisposable = CompositeDisposable()

    abstract fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult>

    protected fun makeExportFilename(workbook: Workbook, metadata: ResourceMetadata): String {
        val lang = workbook.target.language.slug
        val resource = if (workbook.source.language.slug == workbook.target.language.slug) {
            metadata.identifier
        } else {
            FileNamer.DEFAULT_RC_SLUG
        }
        val project = workbook.target.slug
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
        return "$lang-$resource-$project-$timestamp.zip"
    }

    protected fun restoreFileExtension(file: File, extension: String) {
        val fileName = file.nameWithoutExtension + ".$extension"
        // using nio Files.move() instead of file.rename() for platform independent
        Files.move(
            file.toPath(),
            file.parentFile.resolve(fileName).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
    }

    protected fun setContributorInfo(
        contributors: List<Contributor>,
        metadata: ResourceMetadata,
        projectFile: File
    ) {
        ResourceContainer.load(projectFile).use { rc ->
            rc.manifest.dublinCore.apply {
                contributor = contributors.map { it.name }.toMutableList()
                creator = metadata.creator
            }
            rc.writeManifest()
        }
    }

    protected fun compileCompletedChapters(
        workbook: Workbook,
        projectMetadata: ResourceMetadata,
        projectFilesAccessor: ProjectFilesAccessor
    ): Completable {
        return filterChaptersReadyToCompile(workbook.target.chapters)
            .flatMapCompletable { chapter ->
                // compile the chapter
                chapter.chunks
                    .mapNotNull { chunk -> chunk.audio.selected.value?.value?.file }
                    .toList()
                    .flatMap { takes ->
                        logger.info("Compiling chunk/verse takes for completed chapter #${chapter.sort}")
                        concatenateAudio.execute(takes)
                    }
                    .flatMapCompletable { compiledTake ->
                        logger.info("Importing the new compiled chapter take ${compiledTake.name}")
                        takeActions.import(
                            chapter.audio,
                            projectFilesAccessor.audioDir,
                            createFileNamer(workbook, chapter, projectMetadata.identifier),
                            compiledTake
                        ).andThen(
                            subscribeToSelectedChapter(chapter)
                        )
                    }
            }
            .doOnError {
                logger.error("Error while compiling completed chapters.", it)
            }
            .doFinally {
                compositeDisposable.clear()
            }
            .subscribeOn(Schedulers.io())
    }

    private fun filterChaptersReadyToCompile(
        chapters: Observable<Chapter>
    ): Observable<Chapter> {
        return chapters
            .filter { chapter ->
                // filter chapter without selected take
                chapter.audio.selected.value?.value == null
            }
            .flatMap { chapter ->
                // filter chapter where all its content are ready to compile
                chapter.chunks
                    .all { chunk ->
                        chunk.audio.selected.value?.value != null
                    }
                    .toObservable()
                    .mapNotNull {
                        if (it) chapter else null
                    }
            }
    }

    private fun createFileNamer(
        wb: Workbook,
        chapter: Chapter,
        rcSlug: String
    ): FileNamer {
        return WorkbookFileNamerBuilder.createFileNamer(
            workbook = wb,
            chapter = chapter,
            chunk = null,
            recordable = chapter,
            rcSlug = rcSlug
        )
    }

    private fun subscribeToSelectedChapter(
        chapter: Chapter
    ): Completable {
        return Completable
            .create { emitter ->
                chapter.audio.selected.subscribe {
                    // wait for the new take to be selected in the relay after inserting.
                    if (it.value != null) {
                        emitter.onComplete()
                    }
                }.addTo(compositeDisposable)
            }
            .timeout(2, TimeUnit.SECONDS)
    }
}