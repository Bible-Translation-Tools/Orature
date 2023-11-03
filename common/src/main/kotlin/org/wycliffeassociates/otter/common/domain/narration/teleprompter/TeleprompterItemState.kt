package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class TeleprompterItemState {
    BEGIN_RECORDING,
    RECORD,
    RECORDING_PAUSED,
    RECORD_DISABLED,
    RECORD_ACTIVE,
    RE_RECORD,
    RE_RECORD_ACTIVE,
    RE_RECORDING_PAUSED,
    RE_RECORD_DISABLED
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
        TeleprompterItemState.RE_RECORD_DISABLED
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
            TeleprompterItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
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
        TeleprompterItemState.RE_RECORD,
        TeleprompterItemState.RE_RECORD_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RECORD_ACTIVE -> RecordActiveState
            TeleprompterItemState.RE_RECORD -> ReRecordState
            TeleprompterItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordState : TeleprompterState {
    override val type = TeleprompterItemState.RE_RECORD

    override val validStateTransitions = setOf(
        TeleprompterItemState.RE_RECORD_ACTIVE,
        TeleprompterItemState.RE_RECORD_DISABLED,
    )

    override val disabledState = ReRecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            TeleprompterItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordDisabledState : TeleprompterState {
    override val type = TeleprompterItemState.RE_RECORD_DISABLED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RE_RECORD
    )

    override val disabledState = ReRecordDisabledState

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RE_RECORD -> ReRecordState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordActiveState : TeleprompterState {
    override val type = TeleprompterItemState.RE_RECORD_ACTIVE

    override val validStateTransitions = setOf(
        TeleprompterItemState.RE_RECORDING_PAUSED,
        TeleprompterItemState.RE_RECORD
    )

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RE_RECORDING_PAUSED -> ReRecordPausedState
            TeleprompterItemState.RE_RECORD -> ReRecordState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState = ReRecordDisabledState
}

object ReRecordPausedState : TeleprompterState {
    override val type = TeleprompterItemState.RE_RECORDING_PAUSED

    override val validStateTransitions = setOf(
        TeleprompterItemState.RE_RECORD_ACTIVE,
        TeleprompterItemState.RE_RECORD,
        TeleprompterItemState.RE_RECORD_DISABLED
    )

    override fun changeState(request: TeleprompterItemState): TeleprompterState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            TeleprompterItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            TeleprompterItemState.RE_RECORD -> ReRecordState
            TeleprompterItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState: TeleprompterState
        get() = throw IllegalStateException("Tried to disable a paused re-recording")
}