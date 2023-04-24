package org.wycliffeassociates.otter.jvm.controls.demo.ui.components

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.WorkbookDemo
import tornadofx.*

class WorkbookOptionTableCell() : TableCell<WorkbookDemo, WorkbookDemo>() {

    private lateinit var popupMenu: ContextMenu

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("table-view__action-icon")
        }
    }

    override fun updateItem(item: WorkbookDemo?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        popupMenu = createPopupMenu(
            { },
            { },
            { }
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
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
            action { onOpenAction() }
        }
        val exportOption = MenuItem("Export Book...").apply {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action { onExportAction() }
        }
        val deleteOption = MenuItem("Delete Book").apply {
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