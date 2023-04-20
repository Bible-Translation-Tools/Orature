package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookActionCallback
import tornadofx.FX
import tornadofx.action
import tornadofx.addClass
import tornadofx.button

class WorkbookOptionTableCell(
    private val actionCallback: WorkbookActionCallback
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
        val openOption = MenuItem("Open Book").apply {
            action { onOpenAction() }
        }
        val exportOption = MenuItem("Export Book...").apply {
            action { onExportAction() }
        }
        val deleteOption = MenuItem("Delete Book").apply {
            action { onDeleteAction() }
        }
        return ContextMenu(openOption, exportOption, deleteOption).apply {
            isAutoHide = true
            prefWidth = -1.0
        }
    }
}