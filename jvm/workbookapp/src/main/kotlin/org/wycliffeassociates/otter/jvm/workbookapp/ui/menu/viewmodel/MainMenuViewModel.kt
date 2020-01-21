package org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.io.File

class MainMenuViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val directoryProvider = injector.directoryProvider
    private val pluginRepository = injector.pluginRepository

    private val workbookVM = find<WorkbookViewModel>()
    val disableExportProjectProperty = workbookVM.activeWorkbookProperty.booleanBinding { it == null }

    val editorPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val recorderPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()

    val showExportDialogProperty = SimpleBooleanProperty(false)
    val showImportDialogProperty = SimpleBooleanProperty(false)

    init {
        refreshPlugins()
    }

    fun exportProject(directory: File) {
        showExportDialogProperty.value = true

        ProjectExporter(
            workbookVM.activeResourceMetadata,
            workbookVM.workbook,
            workbookVM.projectAudioDirectory,
            directoryProvider
        ).export(directory)
            .observeOnFx()
            .subscribe { result: ExportResult ->
                showExportDialogProperty.value = false

                result.errorMessage?.let {
                    error(messages["exportError"], it)
                }
            }
    }

    fun importResourceContainer(fileOrDir: File) {
        showImportDialogProperty.value = true

        ImportResourceContainer(
            injector.resourceRepository,
            injector.resourceContainerRepository,
            injector.collectionRepo,
            injector.contentRepository,
            injector.takeRepository,
            injector.languageRepo,
            directoryProvider,
            injector.zipEntryTreeBuilder
        ).import(fileOrDir)
            .observeOnFx()
            .subscribe { result: ImportResult ->
                if (result == ImportResult.SUCCESS) {
                    find<ProjectGridViewModel>().loadProjects()
                }

                showImportDialogProperty.value = false

                result.errorMessage?.let {
                    error(messages["importError"], it)
                }
            }
    }

    fun refreshPlugins() {
        pluginRepository
            .getAll()
            .observeOnFx()
            .doOnSuccess { pluginData ->
                editorPlugins.setAll(pluginData.filter { it.canEdit })
                recorderPlugins.setAll(pluginData.filter { it.canRecord })
            }
            .observeOn(Schedulers.io())
            .flatMapMaybe {
                pluginRepository.getRecorderData()
            }
            .observeOnFx()
            .doOnSuccess {
                selectedRecorderProperty.set(it)
            }
            .observeOn(Schedulers.io())
            .flatMap {
                pluginRepository.getEditorData()
            }
            .observeOnFx()
            .doOnSuccess {
                selectedEditorProperty.set(it)
            }
            .subscribe()
    }

    fun selectEditor(editorData: AudioPluginData) {
        pluginRepository.setEditorData(editorData).subscribe()
        selectedEditorProperty.set(editorData)
    }

    fun selectRecorder(recorderData: AudioPluginData) {
        pluginRepository.setRecorderData(recorderData).subscribe()
        selectedRecorderProperty.set(recorderData)
    }

    /** Null on success, otherwise localized error text. */
    private val ImportResult.errorMessage: String?
        get() {
            return when (this) {
                ImportResult.SUCCESS -> null
                ImportResult.INVALID_RC -> messages["importErrorInvalidRc"]
                ImportResult.INVALID_CONTENT -> messages["importErrorInvalidContent"]
                ImportResult.UNSUPPORTED_CONTENT -> messages["importErrorUnsupportedContent"]
                ImportResult.IMPORT_ERROR -> messages["importErrorImportError"]
                ImportResult.LOAD_RC_ERROR -> messages["importErrorLoadRcError"]
                ImportResult.ALREADY_EXISTS -> messages["importErrorAlreadyExists"]
                ImportResult.UNMATCHED_HELP -> messages["importErrorUnmatchedHelp"]
            }
        }

    /** Null on success, otherwise localized error text. */
    private val ExportResult.errorMessage: String?
        get() {
            return when (this) {
                ExportResult.SUCCESS -> null
                ExportResult.FAILURE -> messages["exportError"]
            }
        }
}