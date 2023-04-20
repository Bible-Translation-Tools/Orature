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
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.event.NavigationRequestEvent
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
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

    private val actionCallback: WorkbookActionCallback = setupWorkbookActionCallback()

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
                setCellFactory { TableHeaderCell() }
                isReorderable = false
            }
            column("Anthology", Workbook::target.getter).apply {
                setCellValueFactory { it.value.target.toProperty() }
                setCellFactory { TableTextCell() }
                isReorderable = false
            }
            column("Progress", Workbook::progress.getter).apply {
                cellValueFactory = PropertyValueFactory(Workbook::progress.name)
                setCellFactory { TableProgressCell() }
                isReorderable = false
                isSortable = false
            }
            columns.add(
                TableColumn<Workbook, Workbook>().apply {
                    setCellValueFactory { SimpleObjectProperty(it.value) }
                    setCellFactory {
                        TableActionCell(actionCallback)
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
                        actionCallback.openWorkbook(workbook)
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

    private fun setupWorkbookActionCallback(): WorkbookActionCallback {
        return object : WorkbookActionCallback {
            override fun openWorkbook(workbook: Workbook) {
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


open class TableTextCell : TableCell<Workbook, Book>() {
    override fun updateItem(item: Book?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = label("Anthology")
    }
}

class TableHeaderCell : TableTextCell() {
    override fun updateItem(item: Book?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = label(item.title) {
            addClass("table-view__title-cell")
        }
    }
}

class TableProgressCell : TableCell<Workbook, Double>() {
    override fun updateItem(item: Double?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = progressbar(item)
    }
}

class TableActionCell(
    private val callback: WorkbookActionCallback
) : TableCell<Workbook, Workbook>() {

    private lateinit var popupMenu: ContextMenu

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon("mdi-dots-horizontal").apply {
            addClass("table-view__action-icon")
        }
    }

    override fun updateItem(item: Workbook?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        popupMenu = setUpPopupMenu(
            { callback.openWorkbook(item) },
            { callback.exportWorkbook(item) },
            { callback.deleteWorkbook(item) }
        )

        graphic = actionButton.apply {
            action {
                val bound = this.boundsInLocal
                val screenBound = this.localToScreen(bound)
                popupMenu.show(
                    FX.primaryStage
                )
                popupMenu.x = screenBound.centerX - popupMenu.width + this.width
                popupMenu.y = screenBound.maxY
            }
        }
    }

    private fun setUpPopupMenu(
        onOpen: () -> Unit,
        onExport: () -> Unit,
        onDelete: () -> Unit
    ): ContextMenu {
        val openOption = MenuItem("Open Book").apply {
            action { onOpen() }
        }
        val exportOption = MenuItem("Export Book...").apply {
            action { onExport() }
        }
        val deleteOption = MenuItem("Delete Book").apply {
            action { onDelete() }
        }
        return ContextMenu(openOption, exportOption, deleteOption).apply {
            isAutoHide = true
            prefWidth = -1.0
        }
    }
}
interface WorkbookActionCallback {
    fun openWorkbook(workbook: Workbook)
    fun deleteWorkbook(workbook: Workbook)
    fun exportWorkbook(workbook: Workbook)
}