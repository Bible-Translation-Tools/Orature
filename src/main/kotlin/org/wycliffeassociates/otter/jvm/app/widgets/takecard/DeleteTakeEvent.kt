package org.wycliffeassociates.otter.jvm.app.widgets.takecard

import javafx.event.Event
import javafx.event.EventType
import org.wycliffeassociates.otter.common.data.workbook.Take

class DeleteTakeEvent(
    type: EventType<DeleteTakeEvent>,
    val take: Take
) : Event(type) {

    companion object {
        val DELETE_TAKE: EventType<DeleteTakeEvent> = EventType("DELETE_TAKE")
    }
}
