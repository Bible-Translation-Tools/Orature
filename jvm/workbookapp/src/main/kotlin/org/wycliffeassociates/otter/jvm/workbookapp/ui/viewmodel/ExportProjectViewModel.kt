package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.ChapterDescriptor
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.exporter.AudioProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.common.domain.project.exporter.IProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.SourceProjectExporter
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.errorMessage
import tornadofx.ViewModel
import tornadofx.get
import java.io.File
import javax.inject.Inject

class ExportProjectViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var workbookRepo: IWorkbookRepository
    @Inject
    lateinit var exportSourceUseCase: SourceProjectExporter

    @Inject
    lateinit var exportBackupUseCase: BackupProjectExporter

    @Inject
    lateinit var exportAudioUseCase: AudioProjectExporter

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun loadAvailableChapters(
        workbookDescriptor: WorkbookDescriptor
    ): Single<List<ChapterDescriptor>> {
        return Single
            .fromCallable {
                workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
            }
            .flatMapObservable { workbook ->
                workbook.target.chapters
                    .filter { it.hasSelectedAudio() }
                    .map { chapter ->
                        val chunkCount = chapter.chunkCount.blockingGet()
                        val chunkWithAudio = chapter.chunks
                            .getValues(emptyArray())
                            .count { it.hasSelectedAudio() }

                        val progress = if (chunkCount != 0) {
                            chunkWithAudio.toDouble() / chunkCount
                        } else {
                            0.0
                        }
                        ChapterDescriptor(chapter.sort, progress)
                    }
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .doOnError { logger.error("Error retrieving chapters to export.", it) }
    }

    fun exportWorkbook(
        workbookDescriptor: WorkbookDescriptor,
        directory: File,
        type: ExportType,
        chapters: List<Int>
    ) {
        val workbook = workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
        val resourceMetadata = workbook.target.resourceMetadata
        val exporter: IProjectExporter = when (type) {
            ExportType.LISTEN -> exportAudioUseCase
            ExportType.SOURCE_AUDIO, ExportType.PUBLISH -> exportSourceUseCase
            ExportType.BACKUP -> exportBackupUseCase
        }

        exporter
            .export(
                directory,
                resourceMetadata,
                workbook,
                ExportOptions(chapters)
            )
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in exporting project for project: ${workbook.target.slug}")
            }
            .subscribe { result: ExportResult ->
                println("Export completed: $result")
                result.errorMessage?.let {
                    tornadofx.error(messages["exportError"], it)
                }
            }
    }
}