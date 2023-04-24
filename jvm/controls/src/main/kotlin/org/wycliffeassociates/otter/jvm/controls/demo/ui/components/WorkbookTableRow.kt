package org.wycliffeassociates.otter.jvm.controls.demo.ui.components

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.jvm.controls.demo.ui.viewmodels.WorkbookDemo


class WorkbookTableRow : TableRow<WorkbookDemo>() {

    override fun updateItem(item: WorkbookDemo?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = false

        setOnMouseClicked {
            // clicking on a row opens workbook

        }
    }
}