package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableSet
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import org.wycliffeassociates.otter.common.data.workbook.ChapterDescriptor
import tornadofx.*

class ExportProjectTableCheckbox(
    private val selectedChapters: ObservableSet<ChapterDescriptor>
) : TableCell<ChapterDescriptor, ChapterDescriptor>() {

    private val graphicNode = CheckBox().apply {
        addClass("wa-checkbox")
        isMouseTransparent = true
        isFocusTraversable = false

        selectedProperty().bind(
            booleanBinding(selectedChapters) {
                selectedChapters.contains(item)
            }
        )
    }

    override fun updateItem(item: ChapterDescriptor?, empty: Boolean) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            return
        }

        graphic = graphicNode
    }
}