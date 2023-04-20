package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableCell
import javafx.scene.control.TableColumn
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.WorkbookOptionTableCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookActionCallback
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

    private val workbookActionCallback: WorkbookActionCallback = setupWorkbookOptionCallback()

    init {
        tryImportStylesheet(resources["/css/control.css"])
        tryImportStylesheet(resources["/css/home-page.css"])
    }

    override val root = vbox {
        addClass("home-page__container")

        tableview(viewModel.workbookList) {
            addClass("wa-table-view", "home__book-table")
            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

            column("Book", Workbook::target.getter).apply {
                setCellValueFactory { it.value.target.toProperty() }
                cellFormat {
                    graphic = label(item.title) {
                        addClass("table-view__title-cell")
                    }
                }
                isReorderable = false
            }
            column("Anthology", Workbook::target.getter).apply {
                setCellValueFactory { it.value.target.toProperty() }
                cellFormat {
                    graphic = label("Anthology") {
                        addClass("table-cell__normal-text")
                    }
                }
                isReorderable = false
            }
            column("Progress", Workbook::progress.getter).apply {
                cellValueFactory = PropertyValueFactory(Workbook::progress.name)
                cellFormat {
                    graphic = progressbar(item) {
                        if (item == 1.0) { addClass("full") }
                    }
                }
                isReorderable = false
            }
            columns.add(
                TableColumn<Workbook, Workbook>().apply {
                    setCellValueFactory { SimpleObjectProperty(it.value) }
                    setCellFactory {
                        WorkbookOptionTableCell(workbookActionCallback)
                    }

                    maxWidth = 100.0
                    minWidth = 80.0
                    isReorderable = false
                    isResizable = false
                    isSortable = false
                }
            )

            setRowFactory {
                val row = TableRow<Workbook>()
                row.setOnMouseClicked {
                    // clicking on a row opens workbook
                    row.item?.let { workbook ->
                        workbookActionCallback.openWorkbook(workbook)
                    }
                }
                row
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

    private fun setupWorkbookOptionCallback(): WorkbookActionCallback {
        return object : WorkbookActionCallback {
            override fun openWorkbook(workbook: Workbook) {
                // TODO: FIX BUG: WORKBOOK PAGE IS EMPTY AFTER SELECTING A SECOND BOOK
                
                viewModel.selectProject(workbook)
            }

            override fun exportWorkbook(workbook: Workbook) {
                println("EXPORT NOT IMPLEMENTED")
            }

            override fun deleteWorkbook(workbook: Workbook) {
                println("DELETE NOT IMPLEMENTED")
            }
        }
    }
}
