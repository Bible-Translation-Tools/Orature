package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class TeleprompterItemState {
    BEGIN_RECORDING,
    RECORD,
    RECORDING_PAUSED,
    RECORD_DISABLED,
    RECORD_ACTIVE,
    RECORD_AGAIN,
    RECORD_AGAIN_ACTIVE,
    RECORD_AGAIN_PAUSED,
    RECORD_AGAIN_DISABLED
}

interface TeleprompterState {
    val type: TeleprompterItemState
    val validStateTransitions: Set<TeleprompterItemState>
    val disabledState: TeleprompterState

    fun changeState(request: TeleprompterItemState): TeleprompterState
}

object BeginRecordingState : TeleprompterState {
    override val type = TeleprompterItemState.BEGIN_RECORDING

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD,
        TeleprompterItemState.RECORD_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD -> RecordState
            TeleprompterItemState.RECORD_DISABLED -> RecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD,
        TeleprompterItemState.RECORD_ACTIVE,
        TeleprompterItemState.RECORD_DISABLED,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD -> RecordState
            TeleprompterItemState.RECORD_ACTIVE -> RecordActiveState
            TeleprompterItemState.RECORD_DISABLED -> RecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordDisabledState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_DISABLED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD,
        TeleprompterItemState.RECORD_ACTIVE
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD -> RecordState
            TeleprompterItemState.RECORD_ACTIVE -> RecordActiveState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordActiveState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_ACTIVE

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORDING_PAUSED,
        TeleprompterItemState.RECORD_AGAIN_DISABLED
    )

    override val disabledState: TeleprompterState
        get() = throw IllegalStateException("Tried to disable an active recording")

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            // NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            TeleprompterItemState.RECORDING_PAUSED -> RecordPausedState
            TeleprompterItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordPausedState : TeleprompterState {
    override val type = TeleprompterItemState.RECORDING_PAUSED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD_ACTIVE,
        TeleprompterItemState.RECORD_AGAIN,
        TeleprompterItemState.RECORD_AGAIN_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_ACTIVE -> RecordActiveState
            TeleprompterItemState.RECORD_AGAIN -> RecordAgainState
            TeleprompterItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_AGAIN

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD_AGAIN_ACTIVE,
        TeleprompterItemState.RECORD_AGAIN_DISABLED,
    )

    override val disabledState = RecordAgainDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_AGAIN_ACTIVE -> RecordAgainActiveState
            TeleprompterItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainDisabledState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_AGAIN_DISABLED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD_AGAIN
    )

    override val disabledState = RecordAgainDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_AGAIN -> RecordAgainState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainActiveState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_AGAIN_ACTIVE

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD_AGAIN_PAUSED,
        TeleprompterItemState.RECORD_AGAIN
    )

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_AGAIN_PAUSED -> RecordAgainPausedState
            TeleprompterItemState.RECORD_AGAIN -> RecordAgainState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState = RecordAgainDisabledState
}

object RecordAgainPausedState : TeleprompterState {
    override val type = TeleprompterItemState.RECORD_AGAIN_PAUSED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RECORD_AGAIN_ACTIVE,
        TeleprompterItemState.RECORD_AGAIN,
        TeleprompterItemState.RECORD_AGAIN_DISABLED
    )

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_AGAIN_ACTIVE -> RecordAgainActiveState
            TeleprompterItemState.RECORD_AGAIN -> RecordAgainState
            TeleprompterItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState: TeleprompterState
        get() = throw IllegalStateException("Tried to disable a paused re-recording")
}