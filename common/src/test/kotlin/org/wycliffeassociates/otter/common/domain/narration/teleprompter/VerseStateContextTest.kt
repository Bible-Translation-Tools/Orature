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
package org.wycliffeassociates.otter.common.domain.narration.teleprompter

import org.junit.Assert
import org.junit.Test

class VerseStateContextTest {


    @Test
    fun `BeginReocrdingState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, false),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, false),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, true),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, true),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                BeginRecordingState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `BeginRecordingState changeState to RecordState`() {
        // Verify that RECORD action returns RecordState
        val newState = BeginRecordingState.changeState(VerseItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `BeginRecordingState changeState to RecordDisabledState`() {
        // Verify that RECORD_DISABLED action returns RecordDisabledState
        val newState = BeginRecordingState.changeState(VerseItemState.RECORD_DISABLED)
        Assert.assertEquals(RecordDisabledState, newState)
    }

    @Test
    fun `RecordState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, false),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, false),
            Pair(VerseItemState.RECORD_ACTIVE, false),
            Pair(VerseItemState.RECORD_AGAIN, true),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, true),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordState changeState to RecordState`() {
        // Verify that RECORD action returns RecordState
        val newState = RecordState.changeState(VerseItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `RecordState changeState to RecordActiveState`() {
        // Verify that RECORD_ACTIVE action returns RecordActiveState
        val newState = RecordState.changeState(VerseItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordState changeState to RecordDisabledState`() {
        // Verify that RECORD_DISABLED action returns RecordDisabledState
        val newState = RecordState.changeState(VerseItemState.RECORD_DISABLED)
        Assert.assertEquals(RecordDisabledState, newState)
    }

    @Test
    fun `RecordDisabledState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, false),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, false),
            Pair(VerseItemState.RECORD_AGAIN, true),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, true),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordDisabledState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordDisabledState changeState to RecordState`() {
        // Verify that RECORD action returns RecordState
        val newState = RecordDisabledState.changeState(VerseItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `RecordDisabledState changeState to RecordActiveState`() {
        // Verify that RECORD_ACTIVE action returns RecordActiveState
        val newState = RecordDisabledState.changeState(VerseItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordActiveState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, false),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, false),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, false),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordActiveState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordActiveState changeState to RecordPausedState`() {
        // Verify that RECORDING_PAUSED action returns RecordPausedState
        val newState = RecordActiveState.changeState(VerseItemState.RECORDING_PAUSED)
        Assert.assertEquals(RecordPausedState, newState)
    }

    @Test
    fun `RecordActiveState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordActiveState.changeState(VerseItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }


    @Test
    fun `RecordPausedState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, false),
            Pair(VerseItemState.RECORD_AGAIN, false),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, false),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordPausedState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordPausedState changeState to RecordActiveState`() {
        // Verify that RECORD_ACTIVE action returns RecordActiveState
        val newState = RecordPausedState.changeState(VerseItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordPausedState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordPausedState.changeState(VerseItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordPausedState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordPausedState.changeState(VerseItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }


    @Test
    fun `RecordAgainState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, true),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, false),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, false),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordAgainState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordAgainState changeState to RecordAgainActiveState`() {
        // Verify that RECORD_AGAIN_ACTIVE action returns RecordAgainActiveState
        val newState = RecordAgainState.changeState(VerseItemState.RECORD_AGAIN_ACTIVE)
        Assert.assertEquals(RecordAgainActiveState, newState)
    }

    @Test
    fun `RecordAgainState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordAgainState.changeState(VerseItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }

    @Test
    fun `RecordAgainDisabledState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, false),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, true),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordAgainDisabledState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordAgainDisabledState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordAgainDisabledState.changeState(VerseItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordAgainActiveState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, false),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, false),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, true),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordAgainActiveState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordAgainActiveState changeState to RecordAgainPausedState`() {
        // Verify that RECORD_AGAIN_PAUSED action returns RecordAgainPausedState
        val newState = RecordAgainActiveState.changeState(VerseItemState.RECORD_AGAIN_PAUSED)
        Assert.assertEquals(RecordAgainPausedState, newState)
    }

    @Test
    fun `RecordAgainActiveState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordAgainActiveState.changeState(VerseItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }


    @Test
    fun `RecordAgainPausedState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(VerseItemState.BEGIN_RECORDING, true),
            Pair(VerseItemState.RECORD, true),
            Pair(VerseItemState.RECORDING_PAUSED, true),
            Pair(VerseItemState.RECORD_DISABLED, true),
            Pair(VerseItemState.RECORD_ACTIVE, true),
            Pair(VerseItemState.RECORD_AGAIN, false),
            Pair(VerseItemState.RECORD_AGAIN_ACTIVE, false),
            Pair(VerseItemState.RECORD_AGAIN_PAUSED, true),
            Pair(VerseItemState.RECORD_AGAIN_DISABLED, false),
        )

        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordAgainPausedState.changeState(transition.first)
                if (transition.second) {
                    Assert.fail("Error: expected an exception for state transition: ${transition.first}")
                }
            } catch (illegalState: IllegalStateException) {
                if (!transition.second) {
                    Assert.fail("Error: not expecting exception for state transition: ${transition.first}")
                }
            }
        }
    }

    @Test
    fun `RecordAgainPausedState changeState to RecordAgainActiveState`() {
        // Verify that RECORD_AGAIN_ACTIVE action returns RecordAgainActiveState
        val newState = RecordAgainPausedState.changeState(VerseItemState.RECORD_AGAIN_ACTIVE)
        Assert.assertEquals(RecordAgainActiveState, newState)
    }

    @Test
    fun `RecordAgainPausedState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordAgainPausedState.changeState(VerseItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordAgainPausedState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordAgainPausedState.changeState(VerseItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }

}