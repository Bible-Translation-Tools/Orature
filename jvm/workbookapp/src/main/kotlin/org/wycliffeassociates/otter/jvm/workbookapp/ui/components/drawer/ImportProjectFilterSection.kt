package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import io.reactivex.SingleEmitter
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.scene.control.CheckBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.MessageFormat

class ImportProjectFilterSection(
    private val availableChapters: ObservableList<Int>
) : VBox() {

    lateinit var lv: ListView<Int>
    lateinit var result: SingleEmitter<ImportOptions>
    private val selectedChapters = observableSetOf<Int>()
    private var onImportAction: (List<Int>) -> Unit = {}
    private var onCancelAction: () -> Unit = {}

    init {
        addClass("import-filter__container")
        vgrow = Priority.ALWAYS

        label("We found a matching project in Orature. Please select the chapters you want to import from this file.") {
            addClass("import-filter__sub-text")
            minHeight = Region.USE_PREF_SIZE
        }

        hbox {
            checkbox {
                setOnAction {
                    if (this.isSelected) {
                        selectedChapters.addAll(availableChapters)
                    } else {
                        selectedChapters.clear()
                    }
                    lv.refresh()
                }
            }
            label("Select") {
                graphic = FontIcon(MaterialDesign.MDI_MENU_DOWN)
            }
        }

        listview(availableChapters) {
//            addClass("wa-list-view")
            lv = this
            multiSelect(true)

            setCellFactory {
                ImportFilterSelectionCell(selectedChapters)
            }
        }
        hbox {
            button("Import") {
                addClass("btn", "btn--primary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction {
                    onImportAction(selectedChapters.toList())
                    selectedChapters.clear()
                }
            }
            button("Cancel") {
                addClass("btn", "btn--secondary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction {
                    selectedChapters.clear()
                    onCancelAction()
                }
            }
        }
    }

    fun setOnImportAction(action: (List<Int>) -> Unit) {
        onImportAction = action
    }

    fun setOnCancelAction(action: () -> Unit) {
        onCancelAction = action
    }
}

class ImportFilterSelectionCell(
    private val selectedChapters: ObservableSet<Int>
) : ListCell<Int>() {

    val node = CheckBox().apply {
//        addClass("import-filter__list__check-box")

        selectedProperty().onChange {
            if (it) selectedChapters.add(item)
            else selectedChapters.remove(item)
        }
    }

    init {
        addClass("import-filter__list-cell")

        setOnMouseClicked {
            if (graphic != null) {
                node.isSelected = !node.isSelected
            }
        }
    }

    override fun updateItem(item: Int?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = node.apply {
            text = MessageFormat.format(
                messages["chapterTitle"],
                messages["chapter"],
                item
            )
            isSelected = item in selectedChapters
        }
    }
}