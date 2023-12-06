/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ConflictResolution
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.AddFilesView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer.DrawerEventAction
import org.wycliffeassociates.otter.jvm.controls.event.ProjectImportFinishEvent
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.ImportConflictDialog
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class ImportProjectViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ImportProjectViewModel::class.java)

    val settingsViewModel: SettingsViewModel by inject()

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var importProjectProvider : Provider<ImportProjectUseCase>

    val showImportSuccessDialogProperty = SimpleBooleanProperty(false)
    val showImportErrorDialogProperty = SimpleBooleanProperty(false)
    val importErrorMessage = SimpleStringProperty(null)
    val importedProjectTitleProperty = SimpleStringProperty()
    val importedProjectCoverProperty = SimpleObjectProperty<File>()

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()
    val availableChapters = observableListOf<Int>()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun importProject(file: File): Observable<ProgressStatus> {
        return Observable.create<ProgressStatus> { emitter ->
            val callback = setupImportCallback(emitter)
            importProjectProvider.get()
                .import(file, callback)
                .subscribeOn(Schedulers.io())
                .observeOnFx()
                .doOnError { e ->
                    logger.error("Error in importing resource container $file", e)
                }
                .doFinally {
                    importedProjectTitleProperty.set(null)
                    importedProjectCoverProperty.set(null)
                    emitter.onComplete()
                }
                .subscribe { result: ImportResult ->
                    if (result == ImportResult.FAILED) {
                        callback.onError(file.absolutePath)
                    }
                    fire(DrawerEvent(AddFilesView::class, DrawerEventAction.CLOSE))
                }
        }
    }

    fun isSourceAudioProject(file: File) = importProjectProvider.get().isSourceAudioProject(file)

    private fun setupImportCallback(
        emitter: ObservableEmitter<ProgressStatus>
    ): ProjectImporterCallback {
        return object : ProjectImporterCallback {
            override fun onRequestUserInput(): Single<ImportOptions> {
                return Single.just(ImportOptions(confirmed = true))
            }

            override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
                availableChapters.setAll(parameter.options)
                return Single.create { emitter ->
                    find<ImportConflictDialog> {

                        projectNameProperty.set(parameter.name)
                        chaptersProperty.set(parameter.options.size)

                        setOnSubmitAction { resolution ->
                            val importOption = if (resolution == ConflictResolution.OVERRIDE) {
                                // proceed with override
                                ImportOptions(availableChapters)
                            } else {
                                // aborted
                                ImportOptions(chapters = null)
                            }
                            emitter.onSuccess(importOption)
                            runLater { close() }
                        }

                        setOnCloseAction {
                            emitter.onSuccess(ImportOptions(chapters = null))
                            runLater { close() }
                        }

                        orientationProperty.set(settingsViewModel.orientationProperty.value)
                        themeProperty.set(settingsViewModel.appColorMode.value)

                        runLater { open() }
                    }
                }
            }

            override fun onNotifySuccess(language: String?, project: String?, workbookDescriptor: WorkbookDescriptor?) {
                FX.eventbus.fire(
                    ProjectImportFinishEvent(
                        ImportResult.SUCCESS,
                        language = language,
                        project = project,
                        workbookDescriptor = workbookDescriptor
                    )
                )
            }

            override fun onError(filePath: String) {
                FX.eventbus.fire(
                    ProjectImportFinishEvent(ImportResult.FAILED, filePath = filePath)
                )
            }

            override fun onNotifyProgress(localizeKey: String?, message: String?, percent: Double?) {
                emitter.onNext(
                    ProgressStatus(titleKey = localizeKey, titleMessage = message, percent = percent)
                )
            }
        }
    }

    fun isValidImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                logger.error(
                    "(Drag-Drop) Multi-files import is not supported. Input files: $files"
                )
                false
            }
            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                logger.error(
                    "(Drag-Drop) Directory import is not supported. Input path: ${files.first()}"
                )
                false
            }
            files.first().extension !in OratureFileFormat.extensionList -> {
                snackBarObservable.onNext(messages["importInvalidFileError"])
                logger.error(
                    "(Drag-Drop) Invalid import file extension. Input files: ${files.first()}"
                )
                false
            }
            else -> true
        }
    }

    /**
     * Sets the project title and cover art for the import dialog
     *
     * if the resource container has more than one project, the title and cover art will not be set
     *
     * @param rc The resource container file being imported
     */
    fun setProjectInfo(rc: File) {
        try {
            val project = ResourceContainer.load(rc, true).use {
                if (it.manifest.projects.size != 1) {
                    return@use null
                }
                it.project()
            }
            project?.let {
                importProjectProvider.get()
                    .getSourceMetadata(rc)
                    .doOnError {
                        logger.debug("Error in getSourceMetadata: $rc")
                    }
                    .onErrorComplete()
                    .subscribe { resourceMetadata ->
                        resourceMetadata?.let {
                            importedProjectTitleProperty.set(project.title)

                            /* cover art graphic can be reused later by uncommenting */
                            //  val coverArtAccessor = ArtworkAccessor(directoryProvider, it, project.identifier)
                            //  importedProjectCoverProperty.set(
                            //      coverArtAccessor.getArtwork(ImageRatio.FOUR_BY_ONE)?.file
                            //  )
                        }
                    }
            }
        } catch (e: Exception) {
            logger.error("Error in getting info from resource container $rc", e)
        }
    }
}
