package org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.view

import com.github.thomasnield.rxkotlinfx.toObservable
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.scene.control.ListView
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.events.PlayOrPauseEvent
import org.wycliffeassociates.otter.jvm.app.widgets.takecard.resourcetakecard
import tornadofx.*

class TakesListView(
    items: ObservableList<Take>,
    audioPlayer: () -> IAudioPlayer,
    lastPlayOrPauseEvent: SimpleObjectProperty<PlayOrPauseEvent?>
) : ListView<Take>(items) {
    init {
        cellFormat {
            /* Don't use cell caching, because we remove the front node of the take card when it is dragged
                and we don't ever add it back if it was made the selected take. (This is because we create a
                new take card if it was selected.)
             */
            graphic = resourcetakecard(it, audioPlayer(), lastPlayOrPauseEvent.toObservable())

        }
        vgrow = Priority.ALWAYS
        isFocusTraversable = false
        addClass(RecordResourceStyles.takesList)
    }
}