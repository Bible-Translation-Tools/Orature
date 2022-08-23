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

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.License
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import javax.inject.Inject

class Mp3ProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val workbookRepository: IWorkbookRepository
) : ProjectExporter() {

    @Inject
    lateinit var audioExporter: AudioExporter

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
    ): Single<ExportResult> {
        val isBook = projectMetadataToExport.identifier == workbook.target.resourceMetadata.identifier
        return if (isBook) {
            logger.info("Exporting Scripture project as mp3: ${workbook.target.slug}")
            exportBookMp3(directory, workbook, projectFilesAccessor)
        } else {
            logger.info("Exporting help project as mp3: ${workbook.target.slug}")
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
                val metadata = AudioExporter.ExportMetadata(license, contributors)
                audioExporter.exportMp3(takeFile, outputFile, metadata)
            }
            .toSingle {
                ExportResult.SUCCESS
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }
}