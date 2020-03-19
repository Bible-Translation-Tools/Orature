package org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.view

import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import dev.jbs.gridview.control.GridCell
import org.wycliffeassociates.otter.jvm.controls.card.ScriptureTakeCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeCardType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.TakeModel


class ScriptureTakesGridCell(
    private val newRecordingAction: () -> Unit
) : GridCell<Pair<TakeCardType, TakeModel?>>() {

    var rect = Rectangle()
    var takeCard = ScriptureTakeCard()
    var newRecording = NewRecordingCard(newRecordingAction)

    init {
        rect.fill = Color.GRAY
    }

    override fun updateItem(item: Pair<TakeCardType, TakeModel?>?, empty: Boolean) {
        super.updateItem(item, empty)

        if (!empty && item != null) {
            if (item.first == TakeCardType.NEW) {
                graphic = newRecording
            } else if (item.first == TakeCardType.TAKE && item.second != null) {
                val model = item.second!!
                takeCard.takeProperty().set(model.take)
                takeCard.editTextProperty().set(model.editText)
                takeCard.audioPlayerProperty().set(model.audioPlayer)
                takeCard.deleteTextProperty().set(model.deleteText)
                takeCard.playTextProperty().set(model.playText)
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
