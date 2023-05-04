package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup.WorkbookOptionMenu
import tornadofx.*
import tornadofx.FX.Companion.messages

class WorkbookOptionTableCell : TableCell<WorkbookInfo, WorkbookInfo>() {

    private val popupMenu = WorkbookOptionMenu()

    private val actionButton = button {
        addClass("btn", "btn--icon", "btn--borderless")
        graphic = FontIcon(MaterialDesign.MDI_DOTS_HORIZONTAL).apply {
            addClass("table-view__action-icon")
        }
        tooltip(messages["options"])
    }

    private val graphicContent = hbox {
        alignment = Pos.CENTER_RIGHT
        add(actionButton)
    }

    override fun updateItem(item: WorkbookInfo?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            popupMenu.workbookInfoProperty.set(null)
            graphic = null
            return
        }

        popupMenu.workbookInfoProperty.set(item)
        actionButton.setOnAction {
                val bound = this.boundsInLocal
                val screenBound = this.localToScreen(bound)
                popupMenu.show(
                    FX.primaryStage
                )
                popupMenu.x = screenBound.centerX - popupMenu.width + this.width
                popupMenu.y = screenBound.maxY
        }

        graphic = graphicContent
    }
}