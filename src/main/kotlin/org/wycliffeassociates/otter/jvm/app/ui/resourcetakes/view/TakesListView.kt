package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.TakeEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.resourcetakecard
import tornadofx.*

class TakesListView(items: ObservableList<Take>, audioPlayer: () -> IAudioPlayer) : ListView<Take>(items) {
    private val lastTakeEvent: SimpleObjectProperty<TakeEvent?> = SimpleObjectProperty()

    init {
        cellFormat {
            graphic = cache(it.number) {
                resourcetakecard(it, audioPlayer(), lastTakeEvent.toObservable())
            }
        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(RecordResourceStyles.takesList)

        addEventHandler(TakeEvent.PLAY) {
            lastTakeEvent.set(it)
        }
    }
}