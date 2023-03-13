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
package org.wycliffeassociates.otter.common.domain.project.exporter

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.project.ProjectMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import javax.inject.Inject

class AudioProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider
) : IDirectoryExporter {

    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun export(
        outputDirectory: File,
        projectMetadata: ProjectMetadata,
        workbook: Workbook,
        options: ExportOptions?
    ): Single<ExportResult> {
        val projectAccessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            workbook.target.resourceMetadata,
            workbook.target.toCollection()
        )
        val isBook = projectMetadata.resourceSlug == workbook.target.resourceMetadata.identifier
        return if (isBook) {
            logger.info("Exporting Scripture project as mp3: ${workbook.target.slug}")
            exportBookMp3(outputDirectory, workbook, projectAccessor, options)
        } else {
            logger.info("Exporting help project as mp3: ${workbook.target.slug}")
            exportResourceMp3(outputDirectory, projectMetadata, workbook, projectAccessor)
        }
    }

    private fun exportBookMp3(
        directory: File,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor,
        options: ExportOptions?
    ): Single<ExportResult> {
        val outputProjectDir = directory.resolve(workbook.target.slug).apply { mkdirs() }
        val contributors = projectFilesAccessor.getContributorInfo()
        val license = License.get(workbook.target.resourceMetadata.license)

        return workbook.target.chapters
            .filter { chapterFilter(it, options) }
            .flatMapCompletable { chapter ->
                chapter.audio.selected.value!!.value!!.let {
                    val outputFile = outputProjectDir.resolve("chapter-${chapter.sort}.mp3")
                    val metadata = AudioExporter.ExportMetadata(license, contributors)
                    audioExporter.exportMp3(it.file, outputFile, metadata)
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
        projectMetadata: ProjectMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val contributors = projectFilesAccessor.getContributorInfo()
        val license = License.get(workbook.target.resourceMetadata.license)

        val outputProjectDir = directory
            .resolve("${workbook.target.slug}-${projectMetadata.resourceSlug}")
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
                val metadata = AudioExporter.ExportMetadata(license, contributors)
                audioExporter.exportMp3(takeFile, outputFile, metadata)
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun chapterFilter(chapter: Chapter, options: ExportOptions?): Boolean {
        val included = options?.let { chapter.sort in it.chapters } ?: true
        return included && chapter.audio.selected.value?.value != null
    }
}