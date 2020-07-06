package org.wycliffeassociates.otter.jvm.workbookapp.ui.removeplugins.view

import com.jfoenix.controls.JFXButton
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.workbookapp.ui.removeplugins.viewmodel.RemovePluginsViewModel
import tornadofx.*

class RemovePluginsView : View() {
    private val viewModel: RemovePluginsViewModel by inject()

    init {
        importStylesheet<RemovePluginsStyles>()
    }

    override val root = stackpane {
        title = messages["remove"]
        addClass(RemovePluginsStyles.removePluginsRoot)
        label(messages["noPlugins"]) {
            addClass(RemovePluginsStyles.noPluginLabel)
            visibleProperty().bind(viewModel.noPluginsProperty)
            managedProperty().bind(visibleProperty())
        }
        listview(viewModel.plugins) {
            addClass(RemovePluginsStyles.pluginList)
            visibleProperty().bind(viewModel.noPluginsProperty.not())
            managedProperty().bind(visibleProperty())
            cellCache {
                hbox {
                    addClass(RemovePluginsStyles.pluginListCell)
                    label(it.name) {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                    }
                    add(JFXButton().apply {
                        graphic = RemovePluginsStyles.deleteIcon("20px")
                        isDisableVisualFocus = true
                        addClass(RemovePluginsStyles.deleteButton)
                        action {
                            viewModel.remove(it)
                        }
                    })
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        viewModel.refreshPlugins()
    }
}
