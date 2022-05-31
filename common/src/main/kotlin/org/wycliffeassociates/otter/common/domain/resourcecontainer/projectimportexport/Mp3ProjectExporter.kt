package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
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

    override fun export(
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