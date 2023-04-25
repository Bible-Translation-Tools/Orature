package org.wycliffeassociates.otter.jvm.controls.tableview

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookDeleteEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookExportEvent
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkbookOptionTableCell : TableCell<WorkbookStatus, WorkbookStatus>() {

    private lateinit var popupMenu: ContextMenu

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("table-view__action-icon")
        }
    }

    override fun updateItem(item: WorkbookStatus?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        popupMenu = createPopupMenu(item)

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

    private fun createPopupMenu(workbookStatus: WorkbookStatus): ContextMenu {
        val openOption = MenuItem(messages["openBook"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
            action {
                FX.eventbus.fire(WorkbookOpenEvent(workbookStatus))
            }
        }
        val exportOption = MenuItem(messages["exportProject"]).apply {
            graphic = FontIcon(MaterialDesign.MDI_OPEN_IN_NEW)
            action { FX.eventbus.fire(WorkbookExportEvent(workbookStatus)) }
        }
        val deleteOption = MenuItem(messages["deleteBook"]).apply {
            addClass("danger")
            graphic = FontIcon(MaterialDesign.MDI_DELETE)
            action { FX.eventbus.fire(WorkbookDeleteEvent(workbookStatus)) }
        }
        return ContextMenu(openOption, exportOption, deleteOption).apply {
            isAutoHide = true
            addClass("wa-context-menu")
        }
    }
}
