package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import javafx.scene.control.ToggleGroup
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel

class TakeCell(
    private val toggleGroup: ToggleGroup,
    private val onTakeSelected: (TakeModel) -> Unit
) : ListCell<TakeModel>() {
    private val view = TakeItem()

    override fun updateItem(item: TakeModel?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }
        graphic = view.apply {
            radioGroupProperty.set(toggleGroup)
            selectedProperty.set(item.selected)
            takeProperty.set(item)

            setOnTakeSelected { onTakeSelected(item) }
        }
    }
}
