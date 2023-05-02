package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.WorkbookOptionMenu
import tornadofx.*

class WorkbookOptionTableCell : TableCell<WorkbookInfo, WorkbookInfo>() {

    private val popupMenu = WorkbookOptionMenu()

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("wa-icon")
        }
    }

    override fun updateItem(item: WorkbookInfo?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            popupMenu.workbookInfoProperty.set(null)
            graphic = null
            return
        }

        popupMenu.workbookInfoProperty.set(item)

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