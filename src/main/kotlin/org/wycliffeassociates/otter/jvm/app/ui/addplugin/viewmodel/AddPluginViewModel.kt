package org.wycliffeassociates.otter.jvm.app.ui.addplugin.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.binding.BooleanBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.audioplugin.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.CreatePlugin
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*
import java.io.File

class AddPluginViewModel : ViewModel() {
    private val pluginRepository = Injector.pluginRepository

    var name: String by property("")
    val nameProperty = getProperty(AddPluginViewModel::name)
    var path: String by property("")
    val pathProperty = getProperty(AddPluginViewModel::path)
    var canEdit: Boolean by property(false)
    val canEditProperty = getProperty(AddPluginViewModel::canEdit)
    var canRecord: Boolean by property(false)
    val canRecordProperty = getProperty(AddPluginViewModel::canRecord)

    val plugins: ObservableList<AudioPluginData> = FXCollections.observableArrayList<AudioPluginData>()

    init {
        pluginRepository
                .getAll()
                .observeOnFx()
                .subscribe { retrieved ->
                    plugins.addAll(retrieved)
                }
    }

    fun validateName(): ValidationMessage? {
        return if (name.isEmpty()) validationContext.error("Name cannot be blank") else null
    }

    fun validatePath(): ValidationMessage? {
        return when {
            path.isEmpty() -> validationContext.error("Executable cannot be blank")
            !File(path).exists() -> validationContext.error("Executable not found")
            File(path).isDirectory -> validationContext.error("Executable cannot be a directory")
            else -> null
        }
    }

    fun validated(): BooleanBinding {
        return nameProperty.isNotBlank().and(pathProperty.booleanBinding {
            validatePath() == null
        })
    }

    fun save() {
        // Create the audio plugin
        if (validateName() == null && validatePath() == null) {
            val pluginData = AudioPluginData(
                    0,
                    name,
                    "1.0.0",
                    canEdit,
                    canRecord,
                    path,
                    listOf(),
                    null
            )
            CreatePlugin(pluginRepository)
                    .create(pluginData)
                    .onErrorComplete()
                    .subscribe()
        }
    }
}