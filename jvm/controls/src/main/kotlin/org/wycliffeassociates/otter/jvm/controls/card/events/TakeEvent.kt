package org.wycliffeassociates.otter.jvm.controls.card.events

import javafx.event.EventType
import javafx.scene.input.InputEvent
import org.wycliffeassociates.otter.common.data.workbook.Take

class TakeEvent(
    val take: Take,
    val onComplete: () -> Unit,
    eventType: EventType<out InputEvent>?
) : InputEvent(eventType) {

    companion object {
        private val ANY: EventType<TakeEvent> = EventType<TakeEvent>(InputEvent.ANY, "TAKE")
        val EDIT_TAKE: EventType<TakeEvent> = EventType(ANY, "EDIT_TAKE")
        val MARK_TAKE: EventType<TakeEvent> = EventType(ANY, "MARK_TAKE")

    }
}
