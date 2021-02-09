package org.wycliffeassociates.otter.jvm.workbookapp.ui.removeplugins.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.MyApp
import tornadofx.ViewModel
import javax.inject.Inject

class RemovePluginsViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(RemovePluginsViewModel::class.java)

    @Inject lateinit var pluginRepository: IAudioPluginRepository

    val plugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList()
    val noPluginsProperty: ReadOnlyBooleanProperty

    init {
        (app as MyApp).dependencyGraph.inject(this)
        val listProperty = SimpleListProperty(plugins)
        noPluginsProperty = listProperty.emptyProperty()
    }

    fun refreshPlugins() {
        plugins.clear()
        pluginRepository
            .getAll()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in refreshing plugins", e)
            }
            .subscribe { pluginData ->
                plugins.addAll(pluginData)
            }
    }

    fun remove(plugin: AudioPluginData) {
        plugins.remove(plugin)
        pluginRepository.delete(plugin).subscribe()
    }
}
