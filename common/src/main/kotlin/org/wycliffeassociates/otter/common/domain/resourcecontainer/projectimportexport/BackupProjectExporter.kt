package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import javax.inject.Inject

class BackupProjectExporter @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val workbookRepository: IWorkbookRepository
) : ProjectExporter() {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    override fun export(
        directory: File,
        projectMetadataToExport: ResourceMetadata,
        workbook: Workbook,
        projectFilesAccessor: ProjectFilesAccessor
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
                setContributorInfo(contributors, projectMetadataToExport, zipFile)

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

                restoreFileExtension(zipFile, OratureFileFormat.ORATURE.extension)

                return@fromCallable ExportResult.SUCCESS
            }
            .doOnError {
                logger.error("Failed to export in-progress project", it)
            }
            .onErrorReturnItem(ExportResult.FAILURE)
            .subscribeOn(Schedulers.io())
    }
}