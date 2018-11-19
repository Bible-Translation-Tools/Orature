package org.wycliffeassociates.otter.jvm.app.ui.removeplugins.view

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.jvm.app.ui.removeplugins.viewmodel.RemovePluginsViewModel
import tornadofx.*

class RemovePluginsView : View() {
    private val viewModel: RemovePluginsViewModel by inject()

    init {
        importStylesheet<RemovePluginStyles>()
    }

    override val root = stackpane {
        title = messages["remove"]
        setPrefSize(300.0, 200.0)
        label(messages["noPlugins"]) {
            addClass(RemovePluginStyles.noPluginLabel)
            visibleProperty().bind(viewModel.noPluginsProperty)
            managedProperty().bind(visibleProperty())
        }
        listview(viewModel.plugins) {
            addClass(RemovePluginStyles.pluginList)
            visibleProperty().bind(viewModel.noPluginsProperty.not())
            managedProperty().bind(visibleProperty())
            cellCache {
                hbox(10.0) {
                    addClass(RemovePluginStyles.pluginListCell)
                    label(it.name) {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                    }
                    add(JFXButton().apply {
                        graphic = MaterialIconView(MaterialIcon.DELETE, "20px")
                        isDisableVisualFocus = true
                        addClass(RemovePluginStyles.deleteButton)
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