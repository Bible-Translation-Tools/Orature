package org.wycliffeassociates.otter.jvm.controls.dragtarget.events

import javafx.event.Event
import javafx.event.EventType
import javafx.scene.input.MouseEvent

class AnimateDragEvent(val mouseEvent: MouseEvent) : Event(ANIMATE_DRAG) {
    companion object {
        val ANIMATE_DRAG: EventType<AnimateDragEvent> = EventType("ANIMATE_DRAG")
    }
}
