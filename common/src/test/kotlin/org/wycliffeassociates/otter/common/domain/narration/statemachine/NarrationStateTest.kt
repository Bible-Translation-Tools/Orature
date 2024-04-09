package org.wycliffeassociates.otter.common.domain.narration.statemachine

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.*

class NarrationStateTest {

    @Test
    fun `RecordingState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, false),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, true),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordingState.changeState(transition.first)
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
    fun `RecordingState changeState to RecordingState`() {
        val newState = RecordingState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }


    @Test
    fun `RecordingState changeState to RecordingPausedState`() {
        val newState = RecordingState.changeState(NarrationStateType.RECORDING_PAUSED)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_PAUSED)
    }

    @Test
    fun `RecordingState changeState to MODIFYING_AUDIO_FILE`() {
        val newState = RecordingState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `RecordingPausedState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordingPausedState.changeState(transition.first)
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
    fun `RecordingPausedState changeState to RecordingState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }


    @Test
    fun `RecordingPausedState changeState to InProgressState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `RecordingPausedState changeState to ModifyingAudioState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `RecordingPausedState changeState to PlayingAudioState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }

    @Test
    fun `RecordingPausedState changeState to RecordingAgainState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `RecordingAgainState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, false),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordingAgainState.changeState(transition.first)
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
    fun `RecordingAgainState changeState to ModifyingAudioState`() {
        val newState = RecordingAgainState.changeState(NarrationStateType.RECORDING_AGAIN_PAUSED)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN_PAUSED)
    }


    @Test
    fun `RecordingAgainState changeState to RecordingAgainPausedState`() {
        val newState = RecordingAgainState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `RecordingAgainState changeState to InProgressState`() {
        val newState = RecordingAgainState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `RecordingAgainPausedState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                RecordingAgainPausedState.changeState(transition.first)
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
    fun `RecordingAgainPausedState changeState to ModifyingAudioState`() {
        val newState = RecordingAgainPausedState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `RecordingAgainPausedState changeState to RecordingAgainState`() {
        val newState = RecordingAgainPausedState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `RecordingAgainPausedState changeState to InProgressState`() {
        val newState = RecordingAgainPausedState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `PlayingAudioState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, false),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                PlayingAudioState.changeState(transition.first)
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
    fun `PlayingAudioState changeState to RecordingPausedState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.RECORDING_PAUSED)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_PAUSED)
    }


    @Test
    fun `PlayingAudioState changeState to InProgressState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `PlayingAudioState changeState to FinishedState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.FINISHED)
    }


    @Test
    fun `PlayingAudioState changeState to ModifyingAudioState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `NotStartedState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, true),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                NotStartedState.changeState(transition.first)
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
    fun `NotStartedState changeState to RecordingState`() {
        val newState = NotStartedState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }


    @Test
    fun `NotStartedState changeState to FinishedState`() {
        val newState = NotStartedState.changeState(NarrationStateType.FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.FINISHED)
    }


    @Test
    fun `NotStartedState changeState to InProgressState`() {
        val newState = NotStartedState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `InProgressState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.NOT_STARTED, false),
            Pair(NarrationStateType.IN_PROGRESS, true),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, false),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                InProgressState.changeState(transition.first)
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
    fun `InProgressState changeState to RecordingState`() {
        val newState = InProgressState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }

    @Test
    fun `InProgressState changeState to ModifyingAudioState`() {
        val newState = InProgressState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }

    @Test
    fun `InProgressState changeState to NotStartedState`() {
        val newState = InProgressState.changeState(NarrationStateType.NOT_STARTED)
        Assert.assertEquals(newState.type, NarrationStateType.NOT_STARTED)
    }


    @Test
    fun `InProgressState changeState to RecordingAgainState`() {
        val newState = InProgressState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `InProgressState changeState to PlayingAudioState`() {
        val newState = InProgressState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }


    @Test
    fun `InProgressState changeState to MovingMarkerState`() {
        val newState = InProgressState.changeState(NarrationStateType.MOVING_MARKER)
        Assert.assertEquals(newState.type, NarrationStateType.MOVING_MARKER)
    }


    @Test
    fun `FinishedState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.NOT_STARTED, false),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, false),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                FinishedState.changeState(transition.first)
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
    fun `FinishedState changeState to RecordingAgainState`() {
        val newState = FinishedState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `FinishedState changeState to ModifyingAudioState`() {
        val newState = FinishedState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `FinishedState changeState to NotStartedState`() {
        val newState = FinishedState.changeState(NarrationStateType.NOT_STARTED)
        Assert.assertEquals(newState.type, NarrationStateType.NOT_STARTED)
    }

    @Test
    fun `FinishedState changeState to InProgressState`() {
        val newState = FinishedState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `FinishedState changeState to PlayingAudioState`() {
        val newState = FinishedState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }

    @Test
    fun `FinishedState changeState to MovingMarkerState`() {
        val newState = FinishedState.changeState(NarrationStateType.MOVING_MARKER)
        Assert.assertEquals(newState.type, NarrationStateType.MOVING_MARKER)
    }


    @Test
    fun `ModifyingAudioState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, true),
            Pair(NarrationStateType.MOVING_MARKER, false),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                ModifyingAudioState.changeState(transition.first)
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
    fun `ModifyingAudioState changeState to InProgressState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }

    @Test
    fun `ModifyingAudioState changeState to FinishedState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.FINISHED)
    }

    @Test
    fun `ModifyingAudioState changeState to RecordingAgainState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }

    @Test
    fun `ModifyingAudioState changeState to PlayingAudioState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }


    @Test
    fun `ModifyingAudioState changeState to MovingMarkerState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.MOVING_MARKER)
        Assert.assertEquals(newState.type, NarrationStateType.MOVING_MARKER)
    }


    @Test
    fun `MovingMarkerState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.NOT_STARTED, true),
            Pair(NarrationStateType.IN_PROGRESS, false),
            Pair(NarrationStateType.FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                MovingMarkerState.changeState(transition.first)
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
    fun `MovingMarkerState changeState to InProgressState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IN_PROGRESS)
    }


    @Test
    fun `MovingMarkerState changeState to FinishedState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.FINISHED)
    }


    @Test
    fun `MovingMarkerState changeState to ModifyingAudioState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }
}