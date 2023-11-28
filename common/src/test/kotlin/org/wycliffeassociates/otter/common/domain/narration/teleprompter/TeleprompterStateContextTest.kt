package org.wycliffeassociates.otter.common.domain.narration.teleprompter

import org.junit.Assert
import org.junit.Test

class TeleprompterStateContextTest {


    @Test
    fun `BeginReocrdingState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, false),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, false),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, true),
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
        val newState = BeginRecordingState.changeState(TeleprompterItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `BeginRecordingState changeState to RecordDisabledState`() {
        // Verify that RECORD_DISABLED action returns RecordDisabledState
        val newState = BeginRecordingState.changeState(TeleprompterItemState.RECORD_DISABLED)
        Assert.assertEquals(RecordDisabledState, newState)
    }

    @Test
    fun `RecordState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, false),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, false),
            Pair(TeleprompterItemState.RECORD_ACTIVE, false),
            Pair(TeleprompterItemState.RECORD_AGAIN, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, true),
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
        val newState = RecordState.changeState(TeleprompterItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `RecordState changeState to RecordActiveState`() {
        // Verify that RECORD_ACTIVE action returns RecordActiveState
        val newState = RecordState.changeState(TeleprompterItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordState changeState to RecordDisabledState`() {
        // Verify that RECORD_DISABLED action returns RecordDisabledState
        val newState = RecordState.changeState(TeleprompterItemState.RECORD_DISABLED)
        Assert.assertEquals(RecordDisabledState, newState)
    }

    @Test
    fun `RecordDisabledState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, false),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, false),
            Pair(TeleprompterItemState.RECORD_AGAIN, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, true),
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
        val newState = RecordDisabledState.changeState(TeleprompterItemState.RECORD)
        Assert.assertEquals(RecordState, newState)
    }

    @Test
    fun `RecordDisabledState changeState to RecordActiveState`() {
        // Verify that RECORD_ACTIVE action returns RecordActiveState
        val newState = RecordDisabledState.changeState(TeleprompterItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordActiveState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, false),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, false),
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
        val newState = RecordActiveState.changeState(TeleprompterItemState.RECORDING_PAUSED)
        Assert.assertEquals(RecordPausedState, newState)
    }

    @Test
    fun `RecordActiveState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordActiveState.changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }


    @Test
    fun `RecordPausedState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, false),
            Pair(TeleprompterItemState.RECORD_AGAIN, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, false),
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
        val newState = RecordPausedState.changeState(TeleprompterItemState.RECORD_ACTIVE)
        Assert.assertEquals(RecordActiveState, newState)
    }

    @Test
    fun `RecordPausedState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordPausedState.changeState(TeleprompterItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordPausedState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordPausedState.changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }


    @Test
    fun `RecordAgainState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, false),
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
        val newState = RecordAgainState.changeState(TeleprompterItemState.RECORD_AGAIN_ACTIVE)
        Assert.assertEquals(RecordAgainActiveState, newState)
    }

    @Test
    fun `RecordAgainState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordAgainState.changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }

    @Test
    fun `RecordAgainDisabledState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, true),
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
        val newState = RecordAgainDisabledState.changeState(TeleprompterItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordAgainActiveState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, true),
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
        val newState = RecordAgainActiveState.changeState(TeleprompterItemState.RECORD_AGAIN_PAUSED)
        Assert.assertEquals(RecordAgainPausedState, newState)
    }

    @Test
    fun `RecordAgainActiveState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordAgainActiveState.changeState(TeleprompterItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }


    @Test
    fun `RecordAgainPausedState changeState and all possible state transitions`() {

        // Pair.first = TeleprompterItemState, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(TeleprompterItemState.BEGIN_RECORDING, true),
            Pair(TeleprompterItemState.RECORD, true),
            Pair(TeleprompterItemState.RECORDING_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_DISABLED, true),
            Pair(TeleprompterItemState.RECORD_ACTIVE, true),
            Pair(TeleprompterItemState.RECORD_AGAIN, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_ACTIVE, false),
            Pair(TeleprompterItemState.RECORD_AGAIN_PAUSED, true),
            Pair(TeleprompterItemState.RECORD_AGAIN_DISABLED, false),
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
        val newState = RecordAgainPausedState.changeState(TeleprompterItemState.RECORD_AGAIN_ACTIVE)
        Assert.assertEquals(RecordAgainActiveState, newState)
    }

    @Test
    fun `RecordAgainPausedState changeState to RecordAgainState`() {
        // Verify that RECORD_AGAIN action returns RecordAgainState
        val newState = RecordAgainPausedState.changeState(TeleprompterItemState.RECORD_AGAIN)
        Assert.assertEquals(RecordAgainState, newState)
    }

    @Test
    fun `RecordAgainPausedState changeState to RecordAgainDisabledState`() {
        // Verify that RECORD_AGAIN_DISABLED action returns RecordAgainDisabledState
        val newState = RecordAgainPausedState.changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
        Assert.assertEquals(RecordAgainDisabledState, newState)
    }

}