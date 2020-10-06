package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import dev.jbs.gridview.control.GridCell
import org.wycliffeassociates.otter.jvm.controls.card.EmptyCardCell
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardModel

class ScriptureTakesGridCell(
    newRecordingAction: () -> Unit
) : GridCell<Pair<TakeCardType, TakeCardModel?>>() {

    private var rect = EmptyCardCell()
    private var takeCard = ScriptureTakeCard()
    private var newRecording = NewRecordingCard(newRecordingAction)

    override fun updateItem(item: Pair<TakeCardType, TakeCardModel?>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (!empty && item != null) {
            if (item.first == TakeCardType.NEW) {
                graphic = newRecording
            } else if (
                item.first == TakeCardType.TAKE &&
                item.second != null && !item.second!!.selected
            ) {
                val model = item.second!!
                takeCard.takeProperty().set(model.take)
                takeCard.editTextProperty().set(model.editText)
                takeCard.audioPlayerProperty().set(model.audioPlayer)
                takeCard.deleteTextProperty().set(model.deleteText)
                takeCard.playTextProperty().set(model.playText)
                takeCard.pauseTextProperty().set(model.pauseText)
                takeCard.timestampProperty().set(model.take.createdTimestamp.toString())
                takeCard.takeNumberProperty().set(model.take.number.toString())
                this.graphic = takeCard
            } else {
                rect.heightProperty().bind(heightProperty())
                rect.widthProperty().bind(widthProperty())
                this.graphic = rect
            }
        }
    }
}
