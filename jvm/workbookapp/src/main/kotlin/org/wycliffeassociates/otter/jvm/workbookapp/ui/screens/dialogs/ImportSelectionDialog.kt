package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import io.reactivex.subjects.SingleSubject
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ImportSelectionDialog : OtterDialog() {
    private val settingsViewModel: SettingsViewModel by inject()

    val options = observableListOf<Int>()
    lateinit var result: SingleSubject<ImportOptions>

    private val selectedList = observableListOf<Int>()

    private val content = VBox().apply {
        addClass("confirm-dialog")
        prefWidth = 500.0
        minHeight = 400.0
        paddingAll = 10.0

        hbox {
            hgrow = Priority.ALWAYS

            label ("Import Ongoing Project") {
                addClass("confirm-dialog__title")
            }
            region { hgrow = Priority.ALWAYS }
            button {
                addClass("btn", "btn--secondary")
                graphic = FontIcon("gmi-close")
                action {
                    result.onSuccess(ImportOptions(chapters = null))
                    close()
                }
            }
        }


        label("Please select the chapter(s) you want to import:") {
            addClass("confirm-dialog__message")
        }

        listview(options) {
            multiSelect(true)
            selectedList.bind(selectionModel.selectedItems) { it }

            cellFormat {
                graphic = label(this.item.toString())
            }
        }

        hbox {
            button("Import") {
                addClass("btn", "btn--primary", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_IMPORT)

                action {
                    result.onSuccess(ImportOptions(chapters = selectedList))
                    close()
                }
            }
            region { hgrow = Priority.ALWAYS }
            button("Cancel") {
                addClass("btn", "btn--secondary")
                hgrow = Priority.SOMETIMES

                action {
                    result.onSuccess(ImportOptions(chapters = null))
                    close()
                }
            }
        }


    }

    init {
        setContent(content)
    }

    override fun onDock() {
        super.onDock()
        themeProperty.set(settingsViewModel.appColorMode.value)
        orientationProperty.set(settingsViewModel.orientationProperty.value)
    }

    fun openDialog(resultSubject: SingleSubject<ImportOptions>) {
        result = resultSubject
        open()
    }

    fun closeDialog() {
        options.clear()
        close()
    }
}