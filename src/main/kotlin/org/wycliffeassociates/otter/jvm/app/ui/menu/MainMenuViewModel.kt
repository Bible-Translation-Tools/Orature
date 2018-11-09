package org.wycliffeassociates.otter.jvm.app.ui.menu

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.domain.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.plugins.AccessPlugins
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.ViewModel
import java.io.File

class MainMenuViewModel : ViewModel() {
    private val languageRepo = Injector.languageRepo
    private val metadataRepo = Injector.metadataRepo
    private val collectionRepo = Injector.collectionRepo
    private val chunkRepo = Injector.chunkRepo
    private val directoryProvider = Injector.directoryProvider
    private val pluginRepository = Injector.pluginRepository

    private val accessPlugins = AccessPlugins(pluginRepository)

    val editorPlugins = FXCollections.observableArrayList<AudioPluginData>()
    val recorderPlugins = FXCollections.observableArrayList<AudioPluginData>()
    val selectedEditorProperty = SimpleObjectProperty<AudioPluginData>()
    val selectedRecorderProperty = SimpleObjectProperty<AudioPluginData>()

    init {
        refreshPlugins()
    }

    fun importContainerDirectory(dir: File) {
        val importer = ImportResourceContainer(
                languageRepo,
                metadataRepo,
                collectionRepo,
                chunkRepo,
                directoryProvider
        )
        importer.import(dir)
                .subscribeOn(Schedulers.io()).observeOnFx()
                .subscribe()
    }

    fun refreshPlugins() {
        accessPlugins
                .getAllPluginData()
                .observeOnFx()
                .doOnSuccess { pluginData ->
                    editorPlugins.setAll(pluginData.filter { it.canEdit })
                    recorderPlugins.setAll(pluginData.filter { it.canRecord })
                }
                .observeOn(Schedulers.io())
                .flatMapMaybe {
                    accessPlugins.getRecorderData()
                }
                .observeOnFx()
                .doOnSuccess {
                    selectedRecorderProperty.set(it)
                }
                .observeOn(Schedulers.io())
                .flatMap {
                    accessPlugins.getEditorData()
                }
                .observeOnFx()
                .doOnSuccess {
                    selectedEditorProperty.set(it)
                }
                .subscribe()
    }

    fun selectEditor(editorData: AudioPluginData) {
        accessPlugins.setEditorData(editorData).subscribe()
        selectedEditorProperty.set(editorData)
    }

    fun selectRecorder(recorderData: AudioPluginData) {
        accessPlugins.setRecorderData(recorderData).subscribe()
        selectedRecorderProperty.set(recorderData)
    }
}