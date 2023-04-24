package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.TableRow
import org.wycliffeassociates.otter.common.data.workbook.Workbook

class WorkbookTableRow(
    private val onActionCallback: (Workbook) -> Unit
) : TableRow<Workbook>() {

    override fun updateItem(item: Workbook?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || isEmpty) {
            isMouseTransparent = true
            return
        }

        isMouseTransparent = false

        setOnMouseClicked {
            // clicking on a row opens workbook
            onActionCallback(item)
        }
    }
}