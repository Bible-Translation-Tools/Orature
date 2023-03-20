package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.drawer

import io.reactivex.SingleEmitter
import javafx.collections.ObservableList
import javafx.scene.control.CheckBox
import javafx.scene.control.ListCell
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterSelection
import tornadofx.*

class ImportProjectFilterSection(
    val chapters: ObservableList<ChapterSelection>
) : VBox() {

    lateinit var result: SingleEmitter<ImportOptions>
    private var onImportAction: () -> Unit = {}
    private var onCancelAction: () -> Unit = {}

    init {
        addClass("import-filter__container")
        vgrow = Priority.ALWAYS

        label("We found a matching project in Orature. Please select the chapters you want to import from this file.") {
            addClass("import-filter__sub-text")
            minHeight = Region.USE_PREF_SIZE
//            wrapIn(this@ImportProjectFilterSection)
        }

        listview(chapters) {
//            addClass("wa-list-view")
            multiSelect(true)

            setCellFactory {
                ImportFilterSelectionCell(chapters)
            }
        }
        hbox {
            button("Import") {
                addClass("btn", "btn--primary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction {
                    println(chapters)
                    onImportAction()
                }
            }
            button("Cancel") {
                addClass("btn", "btn--secondary")
                tooltip {
                    textProperty().bind(this@button.textProperty())
                }
                setOnAction { onCancelAction() }
            }
        }
    }

    fun setOnImportAction(action: () -> Unit) {
        onImportAction = action
    }

    fun setOnCancelAction(action: () -> Unit) {
        onCancelAction = action
    }
}

class ImportFilterSelectionCell(
    chapters: ObservableList<ChapterSelection>
) : ListCell<ChapterSelection>() {

    val node = CheckBox().apply {
//        addClass("import-filter__list__check-box")

        selectedProperty().onChange {
            chapters[index].selected = this.isSelected
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

    override fun updateItem(item: ChapterSelection?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = node.apply { text = item.chapter.toString() }
    }
}