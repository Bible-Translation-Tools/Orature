package org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.io.File

class MainMenuViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(MainMenuViewModel::class.java)

    private val injector: Injector by inject()
    private val directoryProvider = injector.directoryProvider
    private val pluginRepository = injector.pluginRepository
    private val workbookRepository = injector.workbookRepository
    private val audioPluginViewModel: AudioPluginViewModel by inject()

    private val workbookVM = find<WorkbookViewModel>()
    val disableExportProjectProperty = workbookVM.activeWorkbookProperty.booleanBinding { it == null }

    val editorPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val recorderPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedMarkerProperty = SimpleObjectProperty<AudioPluginData>()

    val showExportDialogProperty = SimpleBooleanProperty(false)
    val showImportDialogProperty = SimpleBooleanProperty(false)

    init {
        audioPluginViewModel.selectedEditorProperty.bind(selectedEditorProperty)
        audioPluginViewModel.selectedRecorderProperty.bind(selectedRecorderProperty)
        audioPluginViewModel.selectedMarkerProperty.bind(selectedMarkerProperty)
        refreshPlugins()
    }

    fun exportProject(directory: File) {
        showExportDialogProperty.value = true

        ProjectExporter(
            workbookVM.activeResourceMetadata,
            workbookVM.workbook,
            workbookVM.activeProjectFilesAccessor,
            directoryProvider,
            workbookRepository
        ).export(directory)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in exporting project for project: ${workbookVM.workbook.target.slug}")
                logger.error("Project language: ${workbookVM.workbook.target.language.slug}, file: $directory", e)
            }
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
            injector.resourceMetadataRepository,
            injector.resourceContainerRepository,
            injector.collectionRepo,
            injector.contentRepository,
            injector.takeRepository,
            injector.languageRepo,
            directoryProvider,
            injector.zipEntryTreeBuilder,
            injector.resourceRepository
        ).import(fileOrDir)
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in importing resource container $fileOrDir", e)
            }
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
                pluginRepository.getPluginData(PluginType.RECORDER)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedRecorderProperty.set(it)
            }
            .observeOn(Schedulers.io())
            .flatMap {
                pluginRepository.getPluginData(PluginType.EDITOR)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedEditorProperty.set(it)
            }
            .observeOn(Schedulers.io())
            .flatMap {
                pluginRepository.getPluginData(PluginType.MARKER)
            }
            .observeOnFx()
            .doOnSuccess {
                selectedMarkerProperty.set(it)
            }
            .doOnError { e -> logger.error("Error in refreshPlugins", e) }
            .subscribe()
    }

    fun selectEditor(editorData: AudioPluginData) {
        pluginRepository.setPluginData(PluginType.EDITOR, editorData).subscribe()
        selectedEditorProperty.set(editorData)
    }

    fun selectRecorder(recorderData: AudioPluginData) {
        pluginRepository.setPluginData(PluginType.RECORDER, recorderData).subscribe()
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
