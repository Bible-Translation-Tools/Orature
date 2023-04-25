package org.wycliffeassociates.otter.jvm.controls.tableview

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.controlsfx.control.textfield.CustomTextField
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import tornadofx.*

class BookTableView(
    books: ObservableList<WorkbookStatus>
) : VBox() {

    init {
        spacing = 10.0
        hbox {
            spacing = 5.0
            alignment = Pos.CENTER_LEFT
            /* Page title*/
            button {
                addClass("btn", "btn--icon", "btn--borderless")
                graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
                    addClass("table-view__action-icon")
                }
            }
            label("Your English Translations") {
                addClass("home-page__main-header")
            }
            region { hgrow = Priority.ALWAYS }
            hbox {
                /* Search bar */
                alignment = Pos.CENTER_LEFT
                add(
                    CustomTextField().apply {
                        addClass("txt-input", "filtered-search-bar__input")
                        promptText = "Search..."
                        right = FontIcon(MaterialDesign.MDI_MAGNIFY)
                    }
                )
            }
        }
        tableview(books) {
            addClass("wa-table-view", "home__book-table")
            vgrow = Priority.ALWAYS
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

            column("Book", String::class).apply {
                addClass("table-view__column-header-row")
                setCellValueFactory { it.value.title.toProperty() }
                cellFormat {
                    graphic = label(item) {
                        addClass("table-view__title-cell")
                    }
                }
                isReorderable = false
            }
            column("Progress", Number::class) {
                setCellValueFactory { it.value.progress.toProperty() }
                cellFormat {
                    val percent = item.toDouble()
                    graphic = progressbar(percent) {
                        if (percent == 1.0) addClass("full")
                    }
                }
                isReorderable = false
            }
            column("", Boolean::class) {
                setCellValueFactory { SimpleBooleanProperty(it.value.hasSourceAudio) }
                cellFormat {
                    graphic = if (it) {
                        FontIcon(MaterialDesign.MDI_VOLUME_HIGH).apply {
                            addClass("active-icon")
                        }
                    } else {
                        null
                    }
                }
                maxWidth = 50.0
                minWidth = 50.0
                isReorderable = false
                isResizable = false
                isSortable = false
            }
            column("", WorkbookStatus::class) {
                setCellValueFactory { SimpleObjectProperty(it.value) }
                setCellFactory {
                    WorkbookOptionTableCell()
                }

                maxWidth = 100.0
                minWidth = 80.0
                isReorderable = false
                isResizable = false
                isSortable = false
            }

            setRowFactory {
                WorkbookTableRow()
            }
        }
    }
}
