package org.wycliffeassociates.otter.common.domain.narration.statemachine

import org.junit.Assert
import org.junit.Test

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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, true),
            Pair(NarrationStateType.IDLE_FINISHED, true),
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, true),
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
    fun `RecordingPausedState changeState to IdleInProgressState`() {
        val newState = RecordingPausedState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, true),
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
    fun `RecordingAgainState changeState to IdleInProgressState`() {
        val newState = RecordingAgainState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, true),
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
    fun `RecordingAgainPausedState changeState to IdleInProgressState`() {
        val newState = RecordingAgainPausedState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, false),
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
    fun `PlayingAudioState changeState to IdleInProgressState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
    }


    @Test
    fun `PlayingAudioState changeState to IdleFinishedState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.IDLE_FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_FINISHED)
    }


    @Test
    fun `PlayingAudioState changeState to ModifyingAudioState`() {
        val newState = PlayingAudioState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `IdleEmptyState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, true),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, true),
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, true),
            Pair(NarrationStateType.MOVING_MARKER, true),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                IdleEmptyState.changeState(transition.first)
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
    fun `IdleEmptyState changeState to RecordingState`() {
        val newState = IdleEmptyState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }


    @Test
    fun `IdleEmptyState changeState to IdleFinishedState`() {
        val newState = IdleEmptyState.changeState(NarrationStateType.IDLE_FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_FINISHED)
    }


    @Test
    fun `IdleEmptyState changeState to IdleInProgressState`() {
        val newState = IdleEmptyState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
    }


    @Test
    fun `IdleInProgressState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, false),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.IDLE_EMPTY, false),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, true),
            Pair(NarrationStateType.IDLE_FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, false),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                IdleInProgressState.changeState(transition.first)
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
    fun `IdleInProgressState changeState to RecordingState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.RECORDING)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING)
    }

    @Test
    fun `IdleInProgressState changeState to ModifyingAudioState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }

    @Test
    fun `IdleInProgressState changeState to IdleEmptyState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.IDLE_EMPTY)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_EMPTY)
    }


    @Test
    fun `IdleInProgressState changeState to RecordingAgainState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `IdleInProgressState changeState to PlayingAudioState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }


    @Test
    fun `IdleInProgressState changeState to MovingMarkerState`() {
        val newState = IdleInProgressState.changeState(NarrationStateType.MOVING_MARKER)
        Assert.assertEquals(newState.type, NarrationStateType.MOVING_MARKER)
    }


    @Test
    fun `IdleFinishedState changeState and all possible state transitions`() {
        // Pair.first = NarrationStateType, Pair.second = throws exception
        val stateTransitions = mutableListOf(
            Pair(NarrationStateType.RECORDING, true),
            Pair(NarrationStateType.RECORDING_PAUSED, true),
            Pair(NarrationStateType.RECORDING_AGAIN, false),
            Pair(NarrationStateType.RECORDING_AGAIN_PAUSED, true),
            Pair(NarrationStateType.PLAYING, false),
            Pair(NarrationStateType.IDLE_EMPTY, false),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, true),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
            Pair(NarrationStateType.MOVING_MARKER, false),
        )


        // Verifies that exceptions are thrown when invalid states are requested
        for (transition in stateTransitions) {
            try {
                IdleFinishedState.changeState(transition.first)
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
    fun `IdleFinishedState changeState to RecordingAgainState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.RECORDING_AGAIN)
        Assert.assertEquals(newState.type, NarrationStateType.RECORDING_AGAIN)
    }


    @Test
    fun `IdleFinishedState changeState to ModifyingAudioState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }


    @Test
    fun `IdleFinishedState changeState to IdleEmptyState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.IDLE_EMPTY)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_EMPTY)
    }

    @Test
    fun `IdleFinishedState changeState to IdleInProgressState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
    }


    @Test
    fun `IdleFinishedState changeState to PlayingAudioState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.PLAYING)
        Assert.assertEquals(newState.type, NarrationStateType.PLAYING)
    }

    @Test
    fun `IdleFinishedState changeState to MovingMarkerState`() {
        val newState = IdleFinishedState.changeState(NarrationStateType.MOVING_MARKER)
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, false),
            Pair(NarrationStateType.MODIFYING_AUDIO_FILE, false),
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
    fun `ModifyingAudioState changeState to IdleInProgressState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
    }

    @Test
    fun `ModifyingAudioState changeState to IdleFinishedState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.IDLE_FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_FINISHED)
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
    fun `ModifyingAudioState changeState to ModifyingAudioState`() {
        val newState = ModifyingAudioState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
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
            Pair(NarrationStateType.IDLE_EMPTY, true),
            Pair(NarrationStateType.IDLE_IN_PROGRESS, false),
            Pair(NarrationStateType.IDLE_FINISHED, false),
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
    fun `MovingMarkerState changeState to IdleInProgressState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_IN_PROGRESS)
    }


    @Test
    fun `MovingMarkerState changeState to IdleFinishedState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.IDLE_FINISHED)
        Assert.assertEquals(newState.type, NarrationStateType.IDLE_FINISHED)
    }


    @Test
    fun `MovingMarkerState changeState to ModifyingAudioState`() {
        val newState = MovingMarkerState.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        Assert.assertEquals(newState.type, NarrationStateType.MODIFYING_AUDIO_FILE)
    }
}