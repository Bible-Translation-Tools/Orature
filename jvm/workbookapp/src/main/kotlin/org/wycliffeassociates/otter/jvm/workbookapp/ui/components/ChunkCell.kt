package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TakeModel
import tornadofx.*
import java.text.MessageFormat

class ChunkCell(
    private val onChunkOpen: (CardData) -> Unit,
    private val onTakeSelected: (CardData, TakeModel) -> Unit
) : ListCell<CardData>() {
    private val view = ChunkItem()

    override fun updateItem(item: CardData?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = view.apply {
            showTakesProperty.set(false)

            chunkTitleProperty.set(
                MessageFormat.format(
                    FX.messages["chunkTitle"],
                    FX.messages[item.item].capitalize(),
                    item.bodyText
                )
            )

            item.chunkSource?.let { chunk ->
                val selected = chunk.audio.selected.value?.value
                val takeModels = chunk.audio.getAllTakes()
                    .filter { it.deletedTimestamp.value?.value == null }
                    .map { take ->
                        take.mapToModel(take == selected)
                    }
                takes.setAll(takeModels)
            }

            setOnChunkOpen { onChunkOpen(item) }
            setOnTakeSelected { onTakeSelected(item, it) }
        }
    }

    private fun Take.mapToModel(selected: Boolean): TakeModel {
        val audioPlayer = AudioBufferPlayer()
        audioPlayer.load(this.file)
        return TakeModel(this, selected, audioPlayer)
    }
}
