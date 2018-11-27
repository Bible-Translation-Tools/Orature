package org.wycliffeassociates.otter.jvm.app.ui.removeplugins.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.ViewModel

class RemovePluginsViewModel : ViewModel() {
    val pluginRepository = Injector.pluginRepository

    val plugins = FXCollections.observableArrayList<AudioPluginData>()
    val noPluginsProperty: ReadOnlyBooleanProperty

    init {
        val listProperty = SimpleListProperty(plugins)
        noPluginsProperty = listProperty.emptyProperty()
    }

    fun refreshPlugins() {
        plugins.clear()
        pluginRepository
                .getAll()
                .observeOnFx()
                .subscribe { pluginData ->
                    plugins.addAll(pluginData)
                }
    }

    fun remove(plugin: AudioPluginData) {
        plugins.remove(plugin)
        pluginRepository.delete(plugin).subscribe()
    }
}