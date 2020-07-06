package org.wycliffeassociates.otter.jvm.controls.card.events

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class EditTakeEvent(
    val take: Take,
    val onComplete: () -> Unit
) : Event(EDIT_TAKE) {

    companion object {
        val EDIT_TAKE: EventType<EditTakeEvent> = EventType("EDIT_TAKE")
    }
}
