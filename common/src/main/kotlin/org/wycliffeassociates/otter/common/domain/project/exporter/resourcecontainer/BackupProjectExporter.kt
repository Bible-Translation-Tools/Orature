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
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.inProgressNarrationPattern
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.takeFilenamePattern
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ProjectExporterCallback
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import java.lang.Exception
import java.util.regex.Pattern
import java.util.zip.ZipFile
import javax.inject.Inject

class BackupProjectExporter
    @Inject
    constructor(
        directoryProvider: IDirectoryProvider,
        private val workbookRepository: IWorkbookRepository,
    ) : RCProjectExporter(directoryProvider) {
        private val logger = LoggerFactory.getLogger(this.javaClass)

        override fun export(
            outputDirectory: File,
            workbook: Workbook,
            callback: ProjectExporterCallback?,
            options: ExportOptions?,
        ): Single<ExportResult> {
            return Single
                .fromCallable {
                    val resourceMetadata = workbook.target.resourceMetadata
                    val projectSourceMetadata =
                        workbook.source.linkedResources
                            .firstOrNull { it.identifier == resourceMetadata.identifier }
                            ?: workbook.source.resourceMetadata

                    val projectAccessor = workbook.projectFilesAccessor
                    if (!projectAccessor.isInitialized()) {
                        return@fromCallable ExportResult.FAILURE
                    }
                    val contributors = projectAccessor.getContributorInfo()
                    val zipFilename = makeExportFilename(workbook, projectSourceMetadata)
                    val zipFile = outputDirectory.resolve(zipFilename)

                    logger.info("Exporting backup project: ${zipFile.nameWithoutExtension}")

                    projectAccessor.initializeResourceContainerInFile(workbook, zipFile)
                    setContributorInfo(contributors, zipFile)
                    callback?.onNotifyProgress(20.0, messageKey = "exportingTakes")

                    directoryProvider.newFileWriter(zipFile).use { fileWriter ->
                        projectAccessor.copyTakeFiles(
                            fileWriter,
                            workbook,
                            workbookRepository,
                            isBook = true,
                        ) {
                            takesFilter(it, takeFilenamePattern, options)
                        }

                        projectAccessor.copyInProgressNarrationFiles(fileWriter) {
                            takesFilter(it, inProgressNarrationPattern, options)
                        }
                        callback?.onNotifyProgress(70.0, messageKey = "copyingSource")

                        val linkedResource =
                            workbook.source.linkedResources
                                .firstOrNull { it.identifier == resourceMetadata.identifier }

                        projectAccessor.copySourceFilesWithRelatedMedia(
                            fileWriter,
                            directoryProvider.tempDirectory,
                            linkedResource,
                        )
                        callback?.onNotifyProgress(99.0)

                        projectAccessor.writeSelectedTakesFile(
                            fileWriter,
                            workbook,
                            isBook = true,
                        ) { takeName ->
                            takesFilter(takeName, takeFilenamePattern, options)
                        }
                        projectAccessor.writeChunksFile(fileWriter)
                        projectAccessor.copyProjectModeFile(fileWriter)
                        projectAccessor.writeTakeCheckingStatus(fileWriter, workbook) { path ->
                            takesFilter(path, takeFilenamePattern, options)
                        }.blockingAwait()
                    }

                    val exportedFile = restoreFileExtension(zipFile, OratureFileFormat.ORATURE.extension)
                    callback?.onNotifyProgress(100.0)
                    callback?.onNotifySuccess(workbook.target.toCollection(), exportedFile)
                    return@fromCallable ExportResult.SUCCESS
                }
                .doOnError {
                    logger.error("Failed to export in-progress project", it)
                }
                .onErrorReturnItem(ExportResult.FAILURE)
                .subscribeOn(Schedulers.io())
        }

        override fun estimateExportSize(
            workbook: Workbook,
            chapterFilter: List<Int>,
        ): Long {
            var size = 0L
            val projectAccessor = workbook.projectFilesAccessor
            val chapterRegex = Regex("""c(\d+)""")
            val takeDir = projectAccessor.projectDir.resolve(RcConstants.TAKE_DIR)
            val chapterDirs =
                takeDir
                    .listFiles()
                    ?.filter {
                        val chapter = chapterRegex.find(it.name)?.groupValues?.get(1)?.toIntOrNull()
                        chapter in chapterFilter && it.isDirectory
                    }

            chapterDirs?.forEach {
                size += FileUtils.sizeOfDirectory(it)
            }
            size += FileUtils.sizeOfDirectory(projectAccessor.sourceAudioDir)
            size += estimateSourceSize(workbook)

            return size
        }

        /**
         * Estimates the size of source media included in the export file.
         */
        private fun estimateSourceSize(workbook: Workbook): Long {
            val project = workbook.source.slug
            val file = workbook.source.resourceMetadata.path
            var size = 0L

            ZipFile(file).use { zip ->
                zip.entries()
                    .asIterator()
                    .forEach {
                        if (it.name.contains("${RcConstants.SOURCE_MEDIA_DIR}/$project")) {
                            size += it.compressedSize
                        }
                    }
            }

            return size
        }

        private fun takesFilter(
            path: String,
            pattern: Pattern,
            exportOptions: ExportOptions?,
        ): Boolean {
            if (exportOptions == null) {
                return true
            }

            return try {
                pattern
                    .matcher(path)
                    .apply { find() }
                    .group(1)
                    .toInt() in exportOptions.chapters
            } catch (e: Exception) {
                false
            }
        }
    }
