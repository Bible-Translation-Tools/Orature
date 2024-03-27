package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class NarrationStateType {
    RECORDING,
    RECORDING_PAUSED,
    RECORDING_AGAIN,
    RECORDING_AGAIN_PAUSED,
    PLAYING,
    IDLE_EMPTY,
    IDLE_IN_PROGRESS,
    IDLE_FINISHED,
    MODIFYING_AUDIO_FILE,
    MOVING_MARKER,
}


interface NarrationState {
    val type: NarrationStateType
    val validStateTransitions: Set<NarrationStateType>
    fun changeState(request: NarrationStateType): NarrationState
}


object RecordingState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING_PAUSED, // Pause
            NarrationStateType.MODIFYING_AUDIO_FILE, // Save (while recording last verse)
            NarrationStateType.RECORDING, // Next
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_PAUSED -> RecordingPausedState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.RECORDING -> RecordingState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object RecordingPausedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING_PAUSED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING, // Resume
            NarrationStateType.IDLE_IN_PROGRESS, // Next
            NarrationStateType.MODIFYING_AUDIO_FILE, // Save (last verse)
            NarrationStateType.PLAYING, // Playing paused verse
            NarrationStateType.RECORDING_AGAIN, // Record again
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object RecordingAgainState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING_AGAIN

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.MODIFYING_AUDIO_FILE, // Save with all verses recorded
            NarrationStateType.RECORDING_AGAIN_PAUSED, // Pause
            NarrationStateType.IDLE_IN_PROGRESS, // Save without all verses recorded
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.RECORDING_AGAIN_PAUSED -> RecordingAgainPausedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object RecordingAgainPausedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING_AGAIN_PAUSED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.MODIFYING_AUDIO_FILE, // Save (with all verses recorded)
            NarrationStateType.RECORDING_AGAIN, // Resume
            NarrationStateType.IDLE_IN_PROGRESS, // Save (without all verses recorded)
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object PlayingAudioState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.PLAYING

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING_PAUSED, // After playing a verse that was in a recording paused state
            NarrationStateType.IDLE_IN_PROGRESS, // Playing audio completes without all verses recorded
            NarrationStateType.IDLE_FINISHED, // Playing audio completes  with all verses recorded,
            NarrationStateType.MODIFYING_AUDIO_FILE, // Started playing audio while bouncing, and bouncing has not finished
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_PAUSED -> RecordingPausedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object IdleEmptyState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.IDLE_EMPTY

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING, // Record
            NarrationStateType.IDLE_FINISHED, // Undoing a restart chapter
            NarrationStateType.IDLE_IN_PROGRESS, // Redo a recording
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object IdleInProgressState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.IDLE_IN_PROGRESS

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING, // Record
            NarrationStateType.RECORDING_AGAIN, // Record again
            NarrationStateType.MODIFYING_AUDIO_FILE, // Open plugin
            NarrationStateType.IDLE_EMPTY, // Undo record,
            NarrationStateType.PLAYING, // Play verse/chapter
            NarrationStateType.MOVING_MARKER, // Moving verse marker
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.IDLE_EMPTY -> IdleEmptyState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.MOVING_MARKER -> MovingMarkerState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object IdleFinishedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.IDLE_FINISHED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING_AGAIN, // Record again
            NarrationStateType.MODIFYING_AUDIO_FILE, // Undo/redo, save (after record again), return/open from plugin
            NarrationStateType.IDLE_EMPTY, // Restart chapter
            NarrationStateType.IDLE_IN_PROGRESS, // Undo record
            NarrationStateType.PLAYING, // Play verse/chapter
            NarrationStateType.MOVING_MARKER, // Moving verse marker
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.IDLE_EMPTY -> IdleEmptyState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.MOVING_MARKER -> MovingMarkerState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object ModifyingAudioState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.MODIFYING_AUDIO_FILE

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.IDLE_IN_PROGRESS, // After bouncing audio for opening plugin without all verses recorded
            NarrationStateType.IDLE_FINISHED, // After record again/undo/redo
            NarrationStateType.RECORDING_AGAIN, // Starts recording again while bouncing audio
            NarrationStateType.PLAYING, // Plays verse/chapter while bouncing audio
            NarrationStateType.MOVING_MARKER, // Moving markers while bouncing
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.MOVING_MARKER -> MovingMarkerState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object MovingMarkerState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.MOVING_MARKER

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.IDLE_IN_PROGRESS, // Moving marker finished without all verses placed
            NarrationStateType.IDLE_FINISHED, // Moving marker finished with all verses placed
            NarrationStateType.MODIFYING_AUDIO_FILE, // Moving marker finished while bouncing audio
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

}