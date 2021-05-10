package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.CreatePlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.*
import javax.inject.Inject

class AddPluginViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(AddPluginViewModel::class.java)

    @Inject lateinit var pluginRepository: IAudioPluginRepository

    private val settingsViewModel: SettingsViewModel by inject()

    val nameProperty = SimpleStringProperty()
    private val name by nameProperty
    val pathProperty = SimpleStringProperty()
    private val path by pathProperty
    val canEditProperty = SimpleBooleanProperty()
    private val canEdit by canEditProperty
    val canRecordProperty = SimpleBooleanProperty()
    private val canRecord by canRecordProperty

    val validProperty = SimpleBooleanProperty(false)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        validProperty.bind(
            nameProperty.isNotEmpty
                .and(pathProperty.isNotEmpty)
                .and(canRecordProperty.or(canEditProperty))
        )
    }

    fun save() {
        // Create the audio plugin
        if (!validProperty.value) return

        val pluginData = AudioPluginData(
            0,
            name,
            "1.0.0",
            canEdit,
            canRecord,
            false,
            path,
            listOf(),
            null
        )
        CreatePlugin(pluginRepository)
            .create(pluginData)
            .doOnSuccess {
                pluginData.id = it

                settingsViewModel.selectRecorder(pluginData)
                settingsViewModel.selectEditor(pluginData)
                settingsViewModel.refreshPlugins()
            }
            .doOnError { e ->
                logger.error("Error creating a plugin:")
                logger.error("Plugin name: $name, path: $path, record: $canRecord, edit: $canEdit", e)
            }
            .onErrorComplete()
            .subscribe()
    }
}
