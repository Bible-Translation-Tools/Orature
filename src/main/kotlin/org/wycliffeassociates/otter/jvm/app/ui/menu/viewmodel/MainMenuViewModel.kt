package org.wycliffeassociates.otter.jvm.app.ui.menu.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.ViewModel
import tornadofx.get
import java.io.File

class MainMenuViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val resourceContainerRepository = injector.resourceContainerRepository
    private val directoryProvider = injector.directoryProvider
    private val pluginRepository = injector.pluginRepository
    private val zipEntryTreeBuilder = injector.zipEntryTreeBuilder

    val editorPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val recorderPlugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()

    val showImportDialogProperty = SimpleBooleanProperty(false)

    init {
        refreshPlugins()
    }

    fun importResourceContainer(fileOrDir: File) {
        val importer = ImportResourceContainer(
                resourceContainerRepository,
                directoryProvider,
                zipEntryTreeBuilder
        )
        showImportDialogProperty.value = true
        importer.import(fileOrDir)
                .observeOnFx()
                .subscribe { result ->
                    val errorMessage = when (result) {
                        ImportResult.SUCCESS -> null
                        ImportResult.INVALID_RC -> messages["importErrorInvalidRc"]
                        ImportResult.INVALID_CONTENT -> messages["importErrorInvalidContent"]
                        ImportResult.UNSUPPORTED_CONTENT -> messages["importErrorUnsupportedContent"]
                        ImportResult.IMPORT_ERROR -> messages["importErrorImportError"]
                        ImportResult.LOAD_RC_ERROR -> messages["importErrorLoadRcError"]
                        ImportResult.ALREADY_EXISTS -> messages["importErrorAlreadyExists"]
                        ImportResult.UNMATCHED_HELP -> messages["importErrorUnmatchedHelp"]
                    }
                    showImportDialogProperty.value = false
                    errorMessage?.let {
                        tornadofx.error(messages["importError"], it)
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
}