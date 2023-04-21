package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.demo.ui.components.WorkbookOptionTableCell
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.BookTableViewModel
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.WorkbookDemo
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import tornadofx.*

class BookTableFragment : Fragment() {

    private val viewModel: BookTableViewModel by inject()

    init {
        tryImportStylesheet("/css/popup-menu.css")
    }

    override val root = vbox {
        addClass("home-page__container")

        tableview(viewModel.workbookList) {
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
                isReorderable = false
                maxWidth = 50.0
                minWidth = 50.0
                isReorderable = false
                isResizable = false
                isSortable = false
            }
            column("", WorkbookDemo::class) {
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
                val row = TableRow<WorkbookDemo>()
                row.setOnMouseClicked {
                    // clicking on a row opens workbook
                    row.item?.let { workbook ->

                    }
                }
                row
            }
        }

    }
}
