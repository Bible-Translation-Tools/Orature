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
package org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.ProjectMetadata
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.takeFilenamePattern
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import java.lang.Exception
import javax.inject.Inject

class BackupProjectExporter @Inject constructor(
    directoryProvider: IDirectoryProvider,
    private val workbookRepository: IWorkbookRepository
) : RCProjectExporter(directoryProvider) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun export(
        outputDirectory: File,
        projectMetadata: ProjectMetadata,
        workbook: Workbook,
        options: ExportOptions?
    ): Single<ExportResult> {
        return Single
            .fromCallable {
                val projectSourceMetadata = workbook.source.linkedResources
                    .firstOrNull { it.identifier == projectMetadata.resourceSlug }
                    ?: workbook.source.resourceMetadata

                val projectToExportIsBook: Boolean =
                    projectMetadata.resourceSlug == workbook.target.resourceMetadata.identifier

                val projectAccessor = getProjectFileAccessor(workbook)
                val contributors = projectAccessor.getContributorInfo()
                val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
                val zipFile = outputDirectory.resolve(zipFilename)

                logger.info("Exporting backup project: ${zipFile.nameWithoutExtension}")

                projectAccessor.initializeResourceContainerInFile(workbook, zipFile)
                setContributorInfo(contributors, projectMetadata.creator, zipFile)

                directoryProvider.newFileWriter(zipFile).use { fileWriter ->
                    projectAccessor.copyTakeFiles(
                        fileWriter,
                        workbook,
                        workbookRepository,
                        projectToExportIsBook
                    ) {
                        takesFilter(it, options)
                    }

                    val linkedResource = workbook.source.linkedResources
                        .firstOrNull { it.identifier == projectMetadata.resourceSlug }

                    projectAccessor.copySourceFilesWithRelatedMedia(
                        fileWriter, directoryProvider.tempDirectory, linkedResource
                    )
                    projectAccessor.writeSelectedTakesFile(fileWriter, workbook, projectToExportIsBook)
                    projectAccessor.writeChunksFile(fileWriter)
                }

                restoreFileExtension(zipFile, OratureFileFormat.ORATURE.extension)

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                logger.error("Failed to export in-progress project", it)
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }

    private fun takesFilter(path: String, exportOptions: ExportOptions?): Boolean {
        if (exportOptions == null) {
            return true
        }

        return try {
            takeFilenamePattern
                .matcher(path)
                .group(1)
                .toInt() in exportOptions.chapters
        } catch (e: Exception) {
            false
        }
    }
}
