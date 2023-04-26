package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.common.data.workbook.WorkbookInfo
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import tornadofx.FX

class WorkbookTableRow : TableRow<WorkbookInfo>() {

    override fun updateItem(item: WorkbookInfo?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = false

        setOnMouseClicked {
            FX.eventbus.fire(WorkbookOpenEvent(item))
        }
    }
}