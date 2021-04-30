package org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.view

import javafx.application.Platform
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.stage.FileChooser
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.dialog.progressdialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel.MainMenuViewModel
import tornadofx.*
import tornadofx.FX.Companion.messages

class MainMenu : MenuBar() {

    private val viewModel: MainMenuViewModel = find()

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
            }
        }
    }
}
