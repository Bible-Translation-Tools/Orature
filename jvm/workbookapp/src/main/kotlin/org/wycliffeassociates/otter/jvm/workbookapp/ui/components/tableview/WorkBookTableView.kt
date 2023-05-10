package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.ProjectInfo
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkBookTableView(
    books: ObservableList<ProjectInfo>
) : TableView<ProjectInfo>(books) {

    init {
        addClass("wa-table-view")
        vgrow = Priority.ALWAYS
        columnResizePolicy = CONSTRAINED_RESIZE_POLICY

        column(messages["book"], String::class).apply {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.title.toProperty() }
            cellFormat {
                graphic = label(item) {
                    addClass("table-view__title-cell")
                    tooltip(item)
                }
            }
            prefWidthProperty().bind(this@WorkBookTableView.widthProperty().multiply(0.25))
            minWidth = 120.0 // this may not be replaced with css
        }
        column(messages["code"], String::class).apply {
            addClass("table-view__column-header-row")
            setCellValueFactory { it.value.slug.toProperty() }
            cellFormat {
                graphic = label(item)
            }
            minWidth = 80.0 // this may not be replaced with css
        }
        column(messages["progress"], Number::class) {
            setCellValueFactory { it.value.progress.toProperty() }
            cellFormat {
                val percent = item.toDouble()
                graphic = progressbar(percent) {
                    if (percent == 1.0) addClass("full")
                }
            }
        }
        column("", Boolean::class) {
            addClass("table-column__status-icon-col")
            setCellValueFactory { SimpleBooleanProperty(it.value.hasSourceAudio) }
            setCellFactory { WorkbookSourceAudioTableCell() }
        }
        column("", ProjectInfo::class) {
            setCellValueFactory { SimpleObjectProperty(it.value) }
            setCellFactory {
                WorkbookOptionTableCell()
            }

            isSortable = false
        }

        setRowFactory {
            WorkbookTableRow()
        }
    }
}

/**
 * Constructs a workbook table and attach it to the parent.
 */
fun EventTarget.workbookTableView(
    values: ObservableList<ProjectInfo>,
    op: WorkBookTableView.() -> Unit = {}
) = WorkBookTableView(values).attachTo(this, op)