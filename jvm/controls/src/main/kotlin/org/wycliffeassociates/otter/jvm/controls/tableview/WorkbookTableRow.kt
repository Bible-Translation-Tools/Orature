package org.wycliffeassociates.otter.jvm.controls.tableview

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.common.data.workbook.WorkbookStatus
import org.wycliffeassociates.otter.jvm.controls.event.WorkbookOpenEvent
import tornadofx.FX

class WorkbookTableRow : TableRow<WorkbookStatus>() {

    override fun updateItem(item: WorkbookStatus?, empty: Boolean) {
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