/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import org.wycliffeassociates.otter.common.domain.project.ProjectCompletionStatus
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

    @Inject
    lateinit var projectCompletionStatus: ProjectCompletionStatus

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
            .map { workbook ->
                workbook
                    .target
                    .chapters
                    .toList()
                    .map { chapters ->
                        chapters.map { chapter ->
                            val progress = when {
                                chapter.hasSelectedAudio() -> 1.0
                                hasInProgressNarration(workbook, chapter) -> {
                                    projectCompletionStatus.getChapterNarrationProgress(workbook, chapter)
                                }

                                else -> projectCompletionStatus.getChapterTranslationProgress(chapter)
                            }
                            ChapterDescriptor(chapter.sort, progress, progress > 0)
                        }
                    }.blockingGet() // blocking get is required for the .cache() observable to emit
            }
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

    fun getEstimateExportSize(
        workbookDescriptor: WorkbookDescriptor,
        chapters: List<Int>,
        exportType: ExportType
    ): Long {
        val workbook = workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
        return when(exportType) {
            ExportType.BACKUP -> exportBackupUseCase.estimateExportSize(workbook, chapters)
            ExportType.LISTEN -> exportAudioUseCase.estimateExportSize(workbook, chapters)
            ExportType.SOURCE_AUDIO,
            ExportType.PUBLISH -> exportSourceUseCase.estimateExportSize(workbook, chapters)
            else -> 0L
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
