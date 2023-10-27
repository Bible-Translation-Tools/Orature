package org.wycliffeassociates.otter.common.domain.narration

enum class NarrationTextItemState {
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

class NarrationStateContext {
    lateinit var state: NarrationState
        private set


}

interface NarrationState {
    val type: NarrationTextItemState
    val validStateTransitions: Set<NarrationTextItemState>

    fun changeState(request: NarrationTextItemState): NarrationState
    fun disable(): NarrationState
    fun enable(): NarrationState
}

object BeginRecordingState: NarrationState {
    override val type = NarrationTextItemState.BEGIN_RECORDING

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD,
        NarrationTextItemState.RECORD_DISABLED
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
            NarrationTextItemState.RECORD_DISABLED -> RecordDisabledState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = RecordDisabledState
    override fun enable() = this
}

object RecordState: NarrationState {
    override val type = NarrationTextItemState.RECORD

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD_ACTIVE,
        NarrationTextItemState.RECORD_DISABLED,
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            NarrationTextItemState.RECORD_DISABLED -> RecordDisabledState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = RecordDisabledState

    override fun enable() = RecordState
}

object RecordDisabledState: NarrationState {
    override val type = NarrationTextItemState.RECORD_DISABLED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = RecordDisabledState

    override fun enable() = RecordState
}

object RecordActiveState: NarrationState {
    override val type = NarrationTextItemState.RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORDING_PAUSED
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORDING_PAUSED -> RecordPausedState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = throw IllegalStateException("Tried to disable an active recording")

    override fun enable() = throw IllegalStateException("Tried to enable an active recording")
}

object RecordPausedState: NarrationState {
    override val type = NarrationTextItemState.RECORDING_PAUSED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD,
        NarrationTextItemState.RE_RECORD_DISABLED
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            NarrationTextItemState.RE_RECORD -> ReRecordState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = throw IllegalStateException("Tried to disable a paused recording")

    override fun enable() = throw IllegalStateException("Tried to enable a paused recording")
}

object ReRecordState: NarrationState {
    override val type = NarrationTextItemState.RE_RECORD

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD_DISABLED,
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = ReRecordDisabledState

    override fun enable() = ReRecordState
}

object ReRecordDisabledState: NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_DISABLED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD -> ReRecordState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = ReRecordDisabledState

    override fun enable() = ReRecordState
}

object ReRecordActiveState: NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORDING_PAUSED
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORDING_PAUSED -> ReRecordPausedState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = throw IllegalStateException("Tried to disable an active re-recording")

    override fun enable() = throw IllegalStateException("Tried to enable an active re-recording")
}

object ReRecordPausedState: NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD,
        NarrationTextItemState.RE_RECORD_DISABLED
    )

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            NarrationTextItemState.RE_RECORD -> ReRecordState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> { throw IllegalStateException("State: $type tried to transition to state: $request") }
        }
    }

    override fun disable() = throw IllegalStateException("Tried to disable a paused re-recording")

    override fun enable() = throw IllegalStateException("Tried to enable a paused re-recording")
}

//BEGIN_RECORDING,
//RECORD,
//RECORDING_PAUSED,
//RECORD_DISABLED,
//RECORD_ACTIVE,
//RE_RECORD,
//RE_RECORD_ACTIVE,
//RE_RECORDING_PAUSED,
//RE_RECORD_DISABLED
//