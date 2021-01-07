package org.wycliffeassociates.otter.jvm.controls.card.events

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class MarkerTakeEvent(
    val take: Take,
    val onComplete: () -> Unit
) : Event(MARK_TAKE) {

    companion object {
        val MARK_TAKE: EventType<MarkerTakeEvent> = EventType("MARK_TAKE")
    }
}
