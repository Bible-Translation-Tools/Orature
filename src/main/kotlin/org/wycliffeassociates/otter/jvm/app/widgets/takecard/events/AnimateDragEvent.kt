package org.wycliffeassociates.otter.jvm.app.widgets.takecard.events

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.input.MouseEvent

class AnimateDragEvent(
    type: EventType<AnimateDragEvent>,
    val mouseEvent: MouseEvent
) : Event(type) {
    companion object {
        val ANIMATE_DRAG: EventType<AnimateDragEvent> = EventType("ANIMATE_DRAG")
    }
}
