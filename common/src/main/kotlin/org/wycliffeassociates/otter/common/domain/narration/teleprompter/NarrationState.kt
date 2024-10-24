package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class NarrationStateType {
    RECORDING,
    RECORDING_PAUSED,
    RECORDING_AGAIN,
    RECORDING_AGAIN_PAUSED,
    PLAYING,
    NOT_STARTED,
    IN_PROGRESS,
    FINISHED,
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
            NarrationStateType.IN_PROGRESS, // Next
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
            NarrationStateType.IN_PROGRESS -> InProgressState
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
            NarrationStateType.IN_PROGRESS, // Save without all verses recorded
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.RECORDING_AGAIN_PAUSED -> RecordingAgainPausedState
            NarrationStateType.IN_PROGRESS -> InProgressState
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
            NarrationStateType.IN_PROGRESS, // Save (without all verses recorded)
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.IN_PROGRESS -> InProgressState
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
            NarrationStateType.IN_PROGRESS, // Playing audio completes without all verses recorded
            NarrationStateType.FINISHED, // Playing audio completes  with all verses recorded,
            NarrationStateType.MODIFYING_AUDIO_FILE, // Started playing audio while bouncing, and bouncing has not finished
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_PAUSED -> RecordingPausedState
            NarrationStateType.IN_PROGRESS -> InProgressState
            NarrationStateType.FINISHED -> FinishedState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object NotStartedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.NOT_STARTED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING, // Record
            NarrationStateType.FINISHED, // Undoing a restart chapter
            NarrationStateType.IN_PROGRESS, // Redo a recording
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.FINISHED -> FinishedState
            NarrationStateType.IN_PROGRESS -> InProgressState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object InProgressState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.IN_PROGRESS

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING, // Record
            NarrationStateType.RECORDING_AGAIN, // Record again
            NarrationStateType.MODIFYING_AUDIO_FILE, // Open plugin
            NarrationStateType.NOT_STARTED, // Undo record,
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
            NarrationStateType.NOT_STARTED -> NotStartedState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.MOVING_MARKER -> MovingMarkerState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object FinishedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.FINISHED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING_AGAIN, // Record again
            NarrationStateType.MODIFYING_AUDIO_FILE, // Undo/redo, save (after record again), return/open from plugin
            NarrationStateType.NOT_STARTED, // Restart chapter
            NarrationStateType.IN_PROGRESS, // Undo record
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
            NarrationStateType.NOT_STARTED -> NotStartedState
            NarrationStateType.IN_PROGRESS -> InProgressState
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
            NarrationStateType.IN_PROGRESS, // After bouncing audio for opening plugin without all verses recorded
            NarrationStateType.FINISHED, // After record again/undo/redo
            NarrationStateType.RECORDING_AGAIN, // Starts recording again while bouncing audio
            NarrationStateType.PLAYING, // Plays verse/chapter while bouncing audio
            NarrationStateType.MOVING_MARKER, // Moving markers while bouncing
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.IN_PROGRESS -> InProgressState
            NarrationStateType.FINISHED -> FinishedState
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
            NarrationStateType.IN_PROGRESS, // Moving marker finished without all verses placed
            NarrationStateType.FINISHED, // Moving marker finished with all verses placed
            NarrationStateType.MODIFYING_AUDIO_FILE, // Moving marker finished while bouncing audio
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.IN_PROGRESS -> InProgressState
            NarrationStateType.FINISHED -> FinishedState
            NarrationStateType.MODIFYING_AUDIO_FILE -> ModifyingAudioState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

}