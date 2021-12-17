/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
