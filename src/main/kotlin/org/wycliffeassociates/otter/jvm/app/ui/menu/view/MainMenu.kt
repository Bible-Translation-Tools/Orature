package org.wycliffeassociates.otter.jvm.app.ui.menu.view

import com.github.thomasnield.rxkotlinfx.toObservable
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.application.Platform
import javafx.scene.control.MenuBar
import javafx.scene.control.ToggleGroup
import org.wycliffeassociates.otter.jvm.app.theme.AppStyles
import org.wycliffeassociates.otter.jvm.app.ui.addplugin.view.AddPluginView
import org.wycliffeassociates.otter.jvm.app.ui.menu.viewmodel.MainMenuViewModel
import org.wycliffeassociates.otter.jvm.app.ui.removeplugins.view.RemovePluginsView
import org.wycliffeassociates.otter.jvm.app.widgets.progressdialog.progressdialog
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainMenu : MenuBar() {

    private val viewModel: MainMenuViewModel = find()

    init {
        importStylesheet<MainMenuStyles>()
        with(this) {
            menu(messages["file"]) {
                item(messages["importResource"]) {
                    graphic = MainMenuStyles.importIcon("20px")
                    val dialog = progressdialog {
                        text = messages["importResource"]
                        graphic = MainMenuStyles.importIcon( "60px")
                        root.addClass(AppStyles.progressDialog)
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
                    graphic = MainMenuStyles.addPluginIcon("20px")
                    action {
                        find<AddPluginView>().apply {
                            whenUndocked { viewModel.refreshPlugins() }
                            openModal()
                        }
                    }
                }
                item(messages["remove"]) {
                    graphic = MainMenuStyles.removePluginIcon("20px")
                    action {
                        find<RemovePluginsView>().apply {
                            whenUndocked { viewModel.refreshPlugins() }
                            openModal()
                        }
                    }
                }
                separator()
                menu(messages["audioRecorder"]) {
                    graphic = MainMenuStyles.recorderIcon("20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.recorderPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.recorderPlugins.map { pluginData ->
                            radiomenuitem(pluginData.name) {
                                userData = pluginData
                                action { if (isSelected) viewModel.selectRecorder(pluginData) }
                                toggleGroup = pluginToggleGroup
                                viewModel.selectedRecorderProperty.toObservable().subscribe {
                                    isSelected = (it == pluginData)
                                }
                            }
                        })
                    }

                }
                menu(messages["audioEditor"]) {
                    graphic = MainMenuStyles.editorIcon("20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.editorPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(viewModel.editorPlugins.map { pluginData ->
                            radiomenuitem(pluginData.name) {
                                userData = pluginData
                                action { if (isSelected) viewModel.selectEditor(pluginData) }
                                toggleGroup = pluginToggleGroup
                                viewModel.selectedEditorProperty.toObservable().subscribe {
                                    isSelected = (it == pluginData)
                                }
                            }
                        })
                    }
                }
            }

        }
    }
}