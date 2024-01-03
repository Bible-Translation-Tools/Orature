package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.tableview

import javafx.collections.ObservableSet
import javafx.scene.control.CheckBox
import javafx.scene.control.TableCell
import org.wycliffeassociates.otter.jvm.controls.model.ChapterDescriptor
import tornadofx.*

class ExportProjectTableActionCell(
    private val selectedChapters: ObservableSet<ChapterDescriptor>,
) : TableCell<ChapterDescriptor, ChapterDescriptor>() {
    private val graphicNode =
        CheckBox().apply {
            addClass("wa-checkbox")
            isMouseTransparent = true
            isFocusTraversable = false
        }

    override fun updateItem(
        item: ChapterDescriptor?,
        empty: Boolean,
    ) {
        super.updateItem(item, empty)

        if (item == null || empty) {
            graphic = null
            graphicNode.selectedProperty().unbind()
            return
        }

        graphic =
            graphicNode.apply {
                isDisable = !item.selectable
                isVisible = item.selectable

                selectedProperty().bind(
                    booleanBinding(selectedChapters) {
                        selectedChapters.contains(item)
                    },
                )
            }
    }
}
