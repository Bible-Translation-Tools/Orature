package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.event.EventTarget
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import org.wycliffeassociates.otter.jvm.controls.model.ChapterDescriptor
import tornadofx.*
import tornadofx.FX.Companion.messages

class ExportProjectTableView(
    chapters: ObservableList<ChapterDescriptor>,
    selectedChapters: ObservableSet<ChapterDescriptor>
) : TableView<ChapterDescriptor>(chapters) {

    private val isSelectedAllProperty = booleanBinding(selectedChapters) { selectedChapters.size == chapters.size }

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
        placeholder = Region()
        sortPolicy = CUSTOM_SORT_POLICY as (Callback<TableView<ChapterDescriptor>, Boolean>)

        column("", ChapterDescriptor::class) {
            addClass("table-view__column-header-row")
            graphic = checkbox {
                addClass("wa-checkbox")
                isSelectedAllProperty.onChange { isSelected = it }
                action {
                    if (isSelected) {
                        selectedChapters.addAll(chapters)
                    } else {
                        selectedChapters.clear()
                    }
                }
            }
            setCellValueFactory { SimpleObjectProperty(it.value) }
            setCellFactory {
                ExportProjectTableActionCell(selectedChapters)
            }
            isReorderable = false
            isSortable = false
            maxWidth = 50.0
            minWidth = 50.0
        }
        column(messages["Chapter"], Int::class) {
            addClass("table-view__column-header-row")
            setCellValueFactory { SimpleObjectProperty(it.value.number) }
            cellFormat {
                graphic = label(item.toString()) {
                    addClass("h4")
                }
            }
            isReorderable = false
            isSortable = true
        }
        column(messages["progress"], Number::class) {
            setCellValueFactory { it.value.progress.toProperty() }
            cellFormat {
                val percent = item.toDouble()
                graphic = progressbar(percent) {
                    if (percent == 1.0) addClass("full")
                    fitToParentWidth()
                }
            }
            isReorderable = false
            isSortable = true
        }

        setRowFactory {
            ExportProjectTableRow(selectedChapters)
        }

        /* accessibility */
        focusedProperty().onChange {
            if (it && selectionModel.selectedIndex < 0) {
                selectionModel.select(0)
                focusModel.focus(0)
            }
        }
        /* handle selection with SPACE and ENTER */
        addEventFilter(KeyEvent.KEY_PRESSED) { event ->
            if (event.code == KeyCode.ENTER || event.code == KeyCode.SPACE) {
                if (selectedItem in selectedChapters) {
                    selectedChapters.remove(selectedItem)
                } else {
                    selectedChapters.add(selectedItem)
                }
            }
        }
    }
}

class ExportProjectTableRow(
    private val selectedChapters: ObservableSet<ChapterDescriptor>
) : TableRow<ChapterDescriptor>() {

    override fun updateItem(item: ChapterDescriptor?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = false

        setOnMouseClicked {
            if (item in selectedChapters) {
                selectedChapters.remove(item)
            } else {
                selectedChapters.add(item)
            }
        }
    }
}

fun EventTarget.exportProjectTableView(
    chapters: ObservableList<ChapterDescriptor>,
    selectedChapters: ObservableSet<ChapterDescriptor>,
    op: ExportProjectTableView.() -> Unit = {}
) = ExportProjectTableView(chapters, selectedChapters).attachTo(this, op)