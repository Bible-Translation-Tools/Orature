package org.wycliffeassociates.otter.jvm.app.ui.menu

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.scene.control.MenuBar
import javafx.scene.control.ToggleGroup
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.removeplugins.view.RemovePluginsView
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainMenu : MenuBar() {

    private val viewModel: MainMenuViewModel = find()

    init {
        with(this) {
            menu(messages["file"]) {
                item(messages["importResource"]) {
                    graphic = MaterialIconView(MaterialIcon.INPUT, "20px")
                    val dialog = progressdialog {
                        text = messages["importResource"]
                        graphic = MaterialIconView(MaterialIcon.INPUT, "60px")
                    }
                    viewModel.showImportDialogProperty.onChange {
                        Platform.runLater { if (it) dialog.open() else dialog.close() }
                    }
                    action {
                        val file = chooseDirectory(messages["importResourceTip"])
                        file?.let {
                            viewModel.importContainerDirectory(file)
                        }
                    }
                }
            }
            menu(messages["audioPlugins"]) {
                item(messages["new"]) {
                    graphic = MaterialIconView(MaterialIcon.ADD, "20px")
                    action {
                        find<AddPluginView>().apply {
                            whenUndocked { viewModel.refreshPlugins() }
                            openModal()
                        }
                    }
                }
                item(messages["remove"]) {
                    graphic = MaterialIconView(MaterialIcon.DELETE, "20px")
                    action {
                        find<RemovePluginsView>().apply {
                            whenUndocked { viewModel.refreshPlugins() }
                            openModal()
                        }
                    }
                }
                separator()
                menu(messages["audioRecorder"]) {
                    graphic = MaterialIconView(MaterialIcon.MIC, "20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.recorderPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.recorderPlugins.map {
                            radiomenuitem(it.name) {
                                userData = it
                                action { if (isSelected) viewModel.selectRecorder(it) }
                                toggleGroup = pluginToggleGroup
                                isSelected = viewModel.selectedRecorderProperty.value == it
                                viewModel.selectedRecorderProperty.onChange {
                                    isSelected = viewModel.selectedRecorderProperty.value == it
                                }
                            }
                        })
                    }

                }
                menu(messages["audioEditor"]) {
                    graphic = MaterialIconView(MaterialIcon.MODE_EDIT, "20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.editorPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.editorPlugins.map {
                            radiomenuitem(it.name) {
                                userData = it
                                action { if (isSelected) viewModel.selectEditor(it) }
                                toggleGroup = pluginToggleGroup
                                isSelected = viewModel.selectedEditorProperty.value == it
                                viewModel.selectedEditorProperty.onChange {
                                    isSelected = viewModel.selectedEditorProperty.value == it
                                }
                            }
                        })
                    }
                }
            }

        }
    }
}