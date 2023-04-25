package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.controls.tableview.WorkbookOptionTableCell
import org.wycliffeassociates.otter.jvm.controls.tableview.WorkbookTableRow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel
import tornadofx.*

class HomePage2 : View() {

    private val navigator: NavigationMediator by inject()
    private val viewModel: HomePageViewModel by inject()
    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["projects"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_HOME))
        action {
            fire(NavigationRequestEvent(this@HomePage2))
        }
    }

    init {
        tryImportStylesheet(resources["/css/control.css"])
        tryImportStylesheet(resources["/css/home-page.css"])
        tryImportStylesheet(resources["/css/popup-menu.css"])
    }

    override val root = vbox {
        addClass("home-page__container")

        tableview(viewModel.workbookList) {
            addClass("wa-table-view", "home__book-table")
            vgrow = Priority.ALWAYS
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

            column(messages["book"], String::class).apply {
                setCellValueFactory { it.value.title.toProperty() }
                cellFormat {
                    graphic = label(item) {
                        addClass("table-view__title-cell")
                    }
                }
                isReorderable = false
            }
            column(messages["progress"], Number::class) {
                setCellValueFactory { it.value.progress.toProperty() }
                cellFormat {
                    val percent = item.toDouble()
                    graphic = progressbar(percent) {
                        if (percent == 1.0) { addClass("full") }
                    }
                }
                isReorderable = false
            }
            column("", Boolean::class) {
                setCellValueFactory { SimpleBooleanProperty(true) }
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

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.dock()
        viewModel.loadBookList()
    }

    override fun onUndock() {
        super.onUndock()
        viewModel.undock()
    }
}
