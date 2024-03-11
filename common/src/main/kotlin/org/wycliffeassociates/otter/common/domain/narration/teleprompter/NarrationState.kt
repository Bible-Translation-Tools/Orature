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
    BOUNCING_AUDIO,
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
            NarrationStateType.BOUNCING_AUDIO, // Save (while recording last verse)
            NarrationStateType.RECORDING, // Next
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_PAUSED -> RecordingPausedState
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.RECORDING -> RecordingState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
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
            NarrationStateType.BOUNCING_AUDIO, // Save (last verse)
            NarrationStateType.PLAYING, // Playing paused verse
            NarrationStateType.RECORDING_AGAIN, // Record again
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.PLAYING -> PlayingAudioState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}


object RecordingAgainState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING_AGAIN

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.BOUNCING_AUDIO, // Save
            NarrationStateType.RECORDING_AGAIN_PAUSED, // Pause
            NarrationStateType.IDLE_IN_PROGRESS
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.RECORDING_AGAIN_PAUSED -> RecordingAgainPausedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}


object RecordingAgainPausedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.RECORDING_AGAIN_PAUSED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.BOUNCING_AUDIO, // Save (with all verses recorded)
            NarrationStateType.RECORDING_AGAIN, // Resume
            NarrationStateType.IDLE_IN_PROGRESS, // Save (without all verses recorded)
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
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
            NarrationStateType.BOUNCING_AUDIO, // Started playing audio while bouncing, and bouncing has not finished
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_PAUSED -> RecordingPausedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
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
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
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
            NarrationStateType.BOUNCING_AUDIO, // Open plugin
            NarrationStateType.IDLE_EMPTY, // Undo record,
            NarrationStateType.PLAYING, // Play verse/chapter
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING -> RecordingState
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.IDLE_EMPTY -> IdleEmptyState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.PLAYING -> PlayingAudioState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}


object IdleFinishedState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.IDLE_FINISHED

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.RECORDING_AGAIN, // Record again
            NarrationStateType.BOUNCING_AUDIO, // Undo/redo, save (after record again), return/open from plugin
            NarrationStateType.IDLE_EMPTY, // Restart chapter
            NarrationStateType.IDLE_IN_PROGRESS, // Undo record
            NarrationStateType.PLAYING, // Play verse/chapter
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.BOUNCING_AUDIO -> BouncingAudioState
            NarrationStateType.IDLE_EMPTY -> IdleEmptyState
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.PLAYING -> PlayingAudioState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}


object BouncingAudioState : NarrationState {
    override val type: NarrationStateType = NarrationStateType.BOUNCING_AUDIO

    override val validStateTransitions: Set<NarrationStateType> =
        setOf(
            NarrationStateType.IDLE_IN_PROGRESS, // After bouncing audio for opening plugin without all verses recorded
            NarrationStateType.IDLE_FINISHED, // After record again/undo/redo
            NarrationStateType.RECORDING_AGAIN, // Starts recording again while bouncing audio
            NarrationStateType.PLAYING, // Plays verse/chapter while bouncing audio
        )

    override fun changeState(request: NarrationStateType): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            NarrationStateType.IDLE_IN_PROGRESS -> IdleInProgressState
            NarrationStateType.IDLE_FINISHED -> IdleFinishedState
            NarrationStateType.RECORDING_AGAIN -> RecordingAgainState
            NarrationStateType.PLAYING -> PlayingAudioState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}