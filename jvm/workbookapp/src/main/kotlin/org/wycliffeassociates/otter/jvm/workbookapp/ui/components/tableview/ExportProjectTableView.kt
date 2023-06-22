package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.collections.ObservableSet
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.util.Callback
import org.wycliffeassociates.otter.common.data.workbook.ChapterSummary
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import tornadofx.FX.Companion.messages

class ExportProjectTableView(
    chapters: ObservableList<ChapterSummary>,
    selectedChapters: ObservableSet<ChapterSummary>
) : TableView<ChapterSummary>(chapters) {

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY
        placeholder = Region()
        sortPolicy = CUSTOM_SORT_POLICY as (Callback<TableView<ChapterSummary>, Boolean>)

        column("", ChapterSummary::class) {
            addClass("table-view__column-header-row")
            setCellValueFactory { SimpleObjectProperty(it.value) }
            cellFormat {
                graphic = checkbox {
                    addClass("wa-checkbox")
                    isSelected = true
                    selectedProperty().onChangeAndDoNow {
                        if (it == true) selectedChapters.add(item) else selectedChapters.remove(item)
                    }
                }
            }
            isReorderable = false
            isSortable = true
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
                }
            }
            isReorderable = false
            isSortable = true
        }

        /* accessibility */
        focusedProperty().onChange {
            if (it && selectionModel.selectedIndex < 0) {
                selectionModel.select(0)
                focusModel.focus(0)
            }
        }

        /* accessibility */
        addEventFilter(KeyEvent.KEY_PRESSED) { keyEvent ->
            if (keyEvent.code == KeyCode.SPACE || keyEvent.code == KeyCode.ENTER) {
                keyEvent.consume()
            }
        }
    }
}

fun EventTarget.exportProjectTableView(
    chapters: ObservableList<ChapterSummary>,
    selectedChapters: ObservableSet<ChapterSummary>,
    op: ExportProjectTableView.() -> Unit = {}
) = ExportProjectTableView(chapters, selectedChapters).attachTo(this, op)