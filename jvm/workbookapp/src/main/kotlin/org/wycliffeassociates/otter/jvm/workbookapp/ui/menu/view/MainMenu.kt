package org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.AddPluginDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel.MainMenuViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs.RemovePluginsDialog
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.workbookapp.controls.dialogs.AppVersionView
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainMenu : MenuBar() {

    private val viewModel: MainMenuViewModel = find()

    private fun Menu.exportMenuItem(message: String): MenuItem {
        return item(message) {
            graphic = MainMenuStyles.exportIcon("20px")
            disableProperty().bind(viewModel.disableExportProjectProperty)
        }
    }

    private fun Menu.importMenuItem(message: String): MenuItem {
        return item(message) {
            graphic = MainMenuStyles.importIcon("20px")
        }
    }

    private fun initImportExportProgressDialog() {
        val importDialog = progressdialog {
            text = messages["importResource"]
            graphic = FontIcon("mdi-import")
        }
        val exportDialog = progressdialog {
            text = messages["exportProject"]
            graphic = FontIcon("mdi-share-variant")
        }
        viewModel.showImportDialogProperty.onChange {
            Platform.runLater { if (it) importDialog.open() else importDialog.close() }
        }
        viewModel.showExportDialogProperty.onChange {
            Platform.runLater { if (it) exportDialog.open() else exportDialog.close() }
        }
    }

    init {
        importStylesheet<MainMenuStyles>()
        initImportExportProgressDialog()
        with(this) {
            menu(messages["file"]) {
                importMenuItem(messages["importResourceFromFolder"])
                    .setOnAction {
                        val file = chooseDirectory(messages["importResourceFromFolder"])
                        file?.let {
                            viewModel.importResourceContainer(file)
                        }
                    }
                importMenuItem(messages["importResourceFromZip"])
                    .setOnAction {
                        val file = chooseFile(
                            messages["importResourceFromZip"],
                            arrayOf(FileChooser.ExtensionFilter("Zip files (*.zip)", "*.zip")),
                            mode = FileChooserMode.Single
                        ).firstOrNull()
                        file?.let {
                            viewModel.importResourceContainer(file)
                        }
                    }
                exportMenuItem(messages["exportProject"])
                    .setOnAction {
                        val directory = chooseDirectory(messages["exportProject"])
                        directory?.let {
                            viewModel.exportWorkbook(it)
                        }
                    }
                separator()
                item(messages["version"]) {
                    setOnAction {
                        find<AppVersionView>().openModal()
                    }
                }
            }
            menu(messages["audioPlugins"]) {
                onShowing = EventHandler {
                    viewModel.refreshPlugins()
                }
                item(messages["new"]) {
                    graphic = MainMenuStyles.addPluginIcon("20px")
                    action {
                        find<AddPluginDialog>().apply {
                            openModal()
                        }
                    }
                }
                item(messages["remove"]) {
                    graphic = MainMenuStyles.removePluginIcon("20px")
                    action {
                        find<RemovePluginsDialog>().apply {
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
                        items.setAll(
                            viewModel.recorderPlugins.map { pluginData ->
                                radiomenuitem(pluginData.name, pluginToggleGroup) {
                                    userData = pluginData
                                    action { if (isSelected) viewModel.selectRecorder(pluginData) }
                                    isSelected = viewModel.selectedRecorderProperty.value == pluginData
                                }
                            }
                        )
                    }
                }
                menu(messages["audioEditor"]) {
                    graphic = MainMenuStyles.editorIcon("20px")
                    val pluginToggleGroup = ToggleGroup()
                    viewModel.editorPlugins.onChange { _ ->
                        items.clear()
                        items.setAll(
                            viewModel.editorPlugins.map { pluginData ->
                                radiomenuitem(pluginData.name, pluginToggleGroup) {
                                    userData = pluginData
                                    action { if (isSelected) viewModel.selectEditor(pluginData) }
                                    isSelected = viewModel.selectedEditorProperty.value == pluginData
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
