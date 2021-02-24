package org.wycliffeassociates.otter.jvm.workbookapp.ui.addplugin.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.binding.BooleanBinding
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.CreatePlugin
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.DependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.MyApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel.MainMenuViewModel
import tornadofx.*
import java.io.File
import javax.inject.Inject

class AddPluginViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(AddPluginViewModel::class.java)

    @Inject lateinit var pluginRepository: IAudioPluginRepository

    private val mainMenuViewModel: MainMenuViewModel by inject()

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
        (app as DependencyGraphProvider).dependencyGraph.inject(this)
        pluginRepository
            .getAll()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting all plugins", e)
            }
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
        return nameProperty
            .isNotBlank()
            .and(
                pathProperty.booleanBinding {
                    validatePath() == null
                }
            )
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
                false,
                path,
                listOf(),
                null
            )
            CreatePlugin(pluginRepository)
                .create(pluginData)
                .doOnSuccess {
                    pluginData.id = it
                    mainMenuViewModel.selectRecorder(pluginData)
                    mainMenuViewModel.selectEditor(pluginData)
                    mainMenuViewModel.refreshPlugins()
                }
                .doOnError { e ->
                    logger.error("Error creating a plugin:")
                    logger.error("Plugin name: $name, path: $path, record: $canRecord, edit: $canEdit", e)
                }
                .onErrorComplete()
                .subscribe()
        }
    }
}
