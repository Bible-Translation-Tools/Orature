package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
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

            column(messages["book"], String::class).apply {
                setCellValueFactory { it.value.target.title.toProperty() }
                cellFormat {
                    graphic = label(item) {
                        addClass("table-view__title-cell")
                    }
                }
                isReorderable = false
            }
            column(messages["anthology"], String::class).apply {
                setCellValueFactory { SimpleStringProperty("") }
                cellFormat {
                    graphic = label("Anthology") {
                        addClass("table-cell__normal-text")
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
            column("", Workbook::class) {
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
