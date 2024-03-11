/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.narration.statemachine

class VerseStateContext {
    lateinit var state: VerseState
        internal set

    private var temporarilyDisabledState: VerseState? = null

    fun changeState(
        request: VerseItemState
    ) {
        temporarilyDisabledState = null
        state = state.changeState(request)
    }

    fun disable() {
        if (temporarilyDisabledState == null) {
            temporarilyDisabledState = state
        }
        state = state.disabledState
    }

    fun restore() {
        state = temporarilyDisabledState ?: state
    }
}