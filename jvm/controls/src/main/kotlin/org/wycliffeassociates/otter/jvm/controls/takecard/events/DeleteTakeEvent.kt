package org.wycliffeassociates.otter.jvm.controls.takecard.events

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class DeleteTakeEvent(val take: Take) : Event(DELETE_TAKE) {
    companion object {
        val DELETE_TAKE: EventType<DeleteTakeEvent> = EventType("DELETE_TAKE")
    }
}
