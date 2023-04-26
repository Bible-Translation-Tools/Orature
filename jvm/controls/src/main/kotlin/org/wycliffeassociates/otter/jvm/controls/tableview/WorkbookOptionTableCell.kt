package org.wycliffeassociates.otter.jvm.controls.tableview

import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import org.wycliffeassociates.otter.jvm.controls.popup.WorkbookOptionMenu
import tornadofx.*

class WorkbookOptionTableCell : TableCell<WorkbookStatus, WorkbookStatus>() {

    private val popupMenu = WorkbookOptionMenu()

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("table-view__action-icon")
        }
    }

    override fun updateItem(item: WorkbookStatus?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            popupMenu.workbookStatusProperty.set(null)
            graphic = null
            return
        }

        popupMenu.workbookStatusProperty.set(item)

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
}