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
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.audio.WAV_TO_MP3_COMPRESSED_RATE
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.utils.mapNotNull
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
        workbook: Workbook,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        val projectAccessor = ProjectFilesAccessor(
            directoryProvider,
            workbook.source.resourceMetadata,
            workbook.target.resourceMetadata,
            workbook.target.toCollection()
        )
        logger.info("Exporting project as mp3: ${workbook.target.slug}")
        return exportBookMp3(outputDirectory, workbook, projectAccessor, callback, options)
    }

    fun estimateExportSize(
        workbook: Workbook,
        chapterFilter: List<Int>
    ): Long {
        return workbook.target.chapters
            .filter { it.sort in chapterFilter }
            .mapNotNull {it.getSelectedTake()?.file }
            .reduce(0L) { size, nextFile ->
                when (AudioFileFormat.of(nextFile.extension)) {
                    AudioFileFormat.MP3 -> size + nextFile.length()
                    AudioFileFormat.WAV -> size + nextFile.length() / WAV_TO_MP3_COMPRESSED_RATE
                    else -> size
                }
            }
            .blockingGet()
    }

    private fun exportBookMp3(
        directory: File,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor,
        callback: ProjectExporterCallback?,
        options: ExportOptions?
    ): Single<ExportResult> {
        callback?.onNotifyProgress(25.0, messageKey = "exportingTakes")
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
                callback?.onNotifyProgress(100.0)
                callback?.onNotifySuccess(workbook.target.toCollection(), outputProjectDir)
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun chapterFilter(chapter: Chapter, options: ExportOptions?): Boolean {
        val included = options?.chapters?.contains(chapter.sort) ?: true
        return included && chapter.hasSelectedAudio()
    }
}