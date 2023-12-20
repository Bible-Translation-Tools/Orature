package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.model.ChapterDescriptor
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.project.exporter.AudioProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportType
import org.wycliffeassociates.otter.common.domain.project.exporter.IProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ProjectExporterCallback
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.SourceProjectExporter
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.ui.events.WorkbookExportFinishEvent
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
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

    @Inject
    lateinit var narrationFactory: NarrationFactory

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun loadChapters(
        workbookDescriptor: WorkbookDescriptor
    ): Single<List<ChapterDescriptor>> {
        return Single
            .fromCallable {
                workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
            }
            .flatMapObservable { workbook ->
                // This is a workaround!
                // Here to trigger getting chapter count.
                // Without it, getting chapter count while we iterate through target chapters doesn't work
                workbook.target.chapters.count().blockingGet()

                workbook.target.chapters
                    .map { chapter ->
                        val chunkCount = chapter.chunkCount.blockingGet()

                        val progress = when {
                            chapter.hasSelectedAudio() -> 1.0
                            hasInProgressNarration(workbook, chapter) -> {
                                val narration = narrationFactory.create(workbook, chapter)

                                val totalVerses = narration.totalVerses.size
                                val activeVerses = narration.activeVerses.size

                                if (totalVerses > 0) {
                                    activeVerses / totalVerses.toDouble()
                                } else 0.0
                            }
                            chunkCount != 0 -> {
                                // collect chunks from the relay as soon as it starts emitting (blocking)
                                val chunkWithAudio = chapter.chunks
                                    .take(1)
                                    .map {
                                        it.count { it.hasSelectedAudio() }
                                    }
                                    .blockingFirst()

                                chunkWithAudio.toDouble() / chunkCount
                            }
                            else -> 0.0
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
        chapters: List<Int>?
    ): Observable<ProgressStatus> {
        val workbook = workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
        val exporter: IProjectExporter = when (type) {
            ExportType.LISTEN -> exportAudioUseCase
            ExportType.SOURCE_AUDIO, ExportType.PUBLISH -> exportSourceUseCase
            ExportType.BACKUP -> exportBackupUseCase
        }
        return Observable.create<ProgressStatus> { emitter ->
            val callback = setUpCallback(emitter)
            exporter
                .export(
                    directory,
                    workbook,
                    callback,
                    chapters?.let { ExportOptions(it) }
                )
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in exporting project for project: ${workbook.target.slug}")
                }
                .doFinally {
                    emitter.onComplete()
                }
                .subscribe { result: ExportResult ->
                    if (result == ExportResult.FAILURE) {
                        callback.onError(workbook.target.toCollection())
                    }
                }
        }
    }

    private fun setUpCallback(emitter: ObservableEmitter<ProgressStatus>): ProjectExporterCallback {
        return object : ProjectExporterCallback {
            override fun onNotifySuccess(project: Collection, file: File) {
                FX.eventbus.fire(
                    WorkbookExportFinishEvent(
                        ExportResult.SUCCESS, project, file
                    )
                )
            }
            override fun onError(project: Collection) {
                FX.eventbus.fire(
                    WorkbookExportFinishEvent(
                        ExportResult.FAILURE, project
                    )
                )
            }

            override fun onNotifyProgress(percent: Double, message: String?) {
                emitter.onNext(
                    ProgressStatus(percent = percent, titleKey = message)
                )
            }
        }
    }

    private fun hasInProgressNarration(workbook: Workbook, chapter: Chapter): Boolean {
        val files = workbook.projectFilesAccessor.getInProgressNarrationFiles(workbook, chapter)
        return files.all { it.exists() }
    }
}
