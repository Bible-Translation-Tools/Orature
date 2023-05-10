package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.geometry.Pos
import javafx.scene.control.TableCell
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import tornadofx.*

class WorkbookSourceAudioTableCell : TableCell<WorkbookDescriptor, Boolean>() {

    private val graphicContent = hbox {
        alignment = Pos.CENTER
        add(
            FontIcon(MaterialDesign.MDI_VOLUME_HIGH).apply {
                addClass("active-icon")
            }
        )
    }

    override fun updateItem(item: Boolean?, empty: Boolean) {
        super.updateItem(item, empty)
        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = graphicContent
    }
}
