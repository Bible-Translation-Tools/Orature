package org.wycliffeassociates.otter.jvm.controls.card.events

import javafx.event.Event
import javafx.event.EventType

sealed class PlayOrPauseEvent(type: EventType<PlayOrPauseEvent>) : Event(type) {

    class PlayEvent : PlayOrPauseEvent(
        PLAY
    )
    class PauseEvent : PlayOrPauseEvent(
        PAUSE
    )

    companion object {
        val PLAY: EventType<PlayOrPauseEvent> = EventType("PLAY")
        val PAUSE: EventType<PlayOrPauseEvent> = EventType("PAUSE")
    }
}
