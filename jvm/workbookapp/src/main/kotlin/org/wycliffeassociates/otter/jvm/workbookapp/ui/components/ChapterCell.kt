package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData

class ChapterCell(metadata: ResourceMetadata, onDelete: () -> Unit): ListCell<CardData?>() {

    private val view = ChapterView(metadata, onDelete)

    override fun updateItem(item: CardData?, empty: Boolean) {
        super.updateItem(item, empty)

        graphic = if (empty) {
            null
        } else {
            view.cardDataProperty.set(item)
            view
        }
    }
}
