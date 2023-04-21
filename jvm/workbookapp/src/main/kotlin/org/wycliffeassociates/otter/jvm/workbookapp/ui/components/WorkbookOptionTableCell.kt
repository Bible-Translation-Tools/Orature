package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookActionCallback
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkbookOptionTableCell(
    private val actionCallback: WorkbookActionCallback
) : TableCell<Workbook, Workbook>() {

    private lateinit var popupMenu: ContextMenu

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("table-view__action-icon")
        }
    }

    override fun updateItem(item: Workbook?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        popupMenu = createPopupMenu(
            { actionCallback.openWorkbook(item) },
            { actionCallback.exportWorkbook(item) },
            { actionCallback.deleteWorkbook(item) }
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

    private fun createPopupMenu(
        onOpenAction: () -> Unit,
        onExportAction: () -> Unit,
        onDeleteAction: () -> Unit
    ): ContextMenu {
        val openOption = MenuItem(messages["openBook"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
            action { onOpenAction() }
        }
        val exportOption = MenuItem(messages["exportProject"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action { onExportAction() }
        }
        val deleteOption = MenuItem(messages["deleteBook"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action { onDeleteAction() }
        }
        return ContextMenu(openOption, exportOption, deleteOption).apply {
            isAutoHide = true
            addClass("wa-context-menu")
        }
    }
}