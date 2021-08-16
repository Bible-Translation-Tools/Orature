package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.jvm.controls.item.ChunkItem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import tornadofx.*
import java.text.MessageFormat

class ChunkCell(private val onChunkOpen: (CardData) -> Unit) : ListCell<CardData>() {
    private val view = ChunkItem()

    override fun updateItem(item: CardData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            chunkTitleProperty.set(
                MessageFormat.format(
                    FX.messages["chunkTitle"],
                    FX.messages[item.item].capitalize(),
                    item.bodyText
                )
            )

            item.chunkSource?.let { chunk ->
                val _takes = chunk.audio.getAllTakes()
                    .filter { it.deletedTimestamp.value?.value == null }
                takes.setAll(_takes)
            }

            showTakesProperty.set(false)

            setOnChunkOpen { onChunkOpen(item) }
        }
    }
}
