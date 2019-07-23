package org.wycliffeassociates.otter.jvm.app.widgets.takecard.events

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class EditTakeEvent(
    type: EventType<EditTakeEvent>,
    val take: Take,
    val onComplete: () -> Unit
) : Event(type) {

    companion object {
        val EDIT_TAKE: EventType<EditTakeEvent> = EventType("EDIT_TAKE")
    }
}