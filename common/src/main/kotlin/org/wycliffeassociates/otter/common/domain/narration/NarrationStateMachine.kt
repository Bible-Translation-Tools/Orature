package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.AudioMarker

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

enum class NarrationStateEventPosition {
    PRECEDING,
    CURRENT,
    SUCCEEDING,
    PERIPHERAL
}

class NarrationStateMachine(
    val total: List<AudioMarker>
) {
    private lateinit var contexts: MutableList<NarrationStateContext>

    fun initialize(active: List<AudioMarker>) {
        if (!::contexts.isInitialized) {
            contexts = total.map { NarrationStateContext() }.toMutableList()
            val recordedIndices = mutableListOf<Int>()
            active.forEach { active ->
                val index = total.indexOfFirst { it.label == active.label }
                if (index != -1) {
                    contexts[index].state = ReRecordState
                    recordedIndices.add(index)
                }
            }
            val lastRecorded = total.indexOfLast { active.last().label == it.label }
            if (lastRecorded != -1 && lastRecorded + 1 <= contexts.lastIndex) {
                contexts[lastRecorded + 1].state = RecordState
                recordedIndices.add(lastRecorded + 1)
            }
            // initial blank recording places a marker with nothing recorded
            if (lastRecorded == 0) {
                contexts[0].state = RecordState
            }
            for (index in contexts.indices) {
                if (index !in recordedIndices) {
                    contexts[index].state = RecordDisabledState
                }
            }
        }
    }

    fun changeState(
        requestIndex: Int,
        request: NarrationTextItemState
    ): List<NarrationTextItemState> {
        if (request !in contexts[requestIndex].state.validStateTransitions) {
            throw IllegalStateException(
                "Could not complete state transition, $request is not a valid transition from ${contexts[requestIndex].state.type}"
            )
        }
        return contexts.mapIndexed { index, context ->
            val orientation = when {
                requestIndex     == index -> NarrationStateEventPosition.CURRENT
                requestIndex - 1 == index -> NarrationStateEventPosition.PRECEDING
                requestIndex + 1 == index -> NarrationStateEventPosition.SUCCEEDING
                else -> NarrationStateEventPosition.PERIPHERAL
            }

            context.changeState(request, orientation)
            context.state.type
        }
    }
}

class NarrationStateContext {
    lateinit var state: NarrationState
        internal set

    private var temporarilyDisabledState: NarrationState? = null

    fun changeState(
        request: NarrationTextItemState,
        position: NarrationStateEventPosition
    ) {
        state = state.changeState(request, position)
    }

    fun disable() {
        temporarilyDisabledState = state
        state = state.disabledState
    }

    fun restore() {
        state = temporarilyDisabledState ?: state
    }
}

interface NarrationState {
    val type: NarrationTextItemState
    val validStateTransitions: Set<NarrationTextItemState>
    val disabledState: NarrationState

    fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState
}

object BeginRecordingState : NarrationState {
    override val type = NarrationTextItemState.BEGIN_RECORDING

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD,
        NarrationTextItemState.RECORD_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
            NarrationTextItemState.RECORD_DISABLED -> RecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordState : NarrationState {
    override val type = NarrationTextItemState.RECORD

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD_ACTIVE,
        NarrationTextItemState.RECORD_DISABLED,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            NarrationTextItemState.RECORD_DISABLED -> RecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordDisabledState : NarrationState {
    override val type = NarrationTextItemState.RECORD_DISABLED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordActiveState : NarrationState {
    override val type = NarrationTextItemState.RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORDING_PAUSED
    )

    override val disabledState: NarrationState
        get() = throw IllegalStateException("Tried to disable an active recording")

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORDING_PAUSED -> RecordPausedState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordPausedState : NarrationState {
    override val type = NarrationTextItemState.RECORDING_PAUSED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD,
        NarrationTextItemState.RE_RECORD_DISABLED
    )

    override val disabledState: NarrationState
        get() = throw IllegalStateException("Tried to disable a paused recording")

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            NarrationTextItemState.RE_RECORD -> ReRecordState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordState : NarrationState {
    override val type = NarrationTextItemState.RE_RECORD

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD_DISABLED,
    )

    override val disabledState = ReRecordDisabledState

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordDisabledState : NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_DISABLED

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD
    )

    override val disabledState = ReRecordDisabledState

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD -> ReRecordState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object ReRecordActiveState : NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORDING_PAUSED
    )

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORDING_PAUSED -> ReRecordPausedState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState: NarrationState
        get() = throw IllegalStateException("Tried to disable an active re-recording")
}

object ReRecordPausedState : NarrationState {
    override val type = NarrationTextItemState.RE_RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RE_RECORD_ACTIVE,
        NarrationTextItemState.RE_RECORD,
        NarrationTextItemState.RE_RECORD_DISABLED
    )

    override fun changeState(request: NarrationTextItemState, position: NarrationStateEventPosition): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RE_RECORD_ACTIVE -> ReRecordActiveState
            NarrationTextItemState.RE_RECORD -> ReRecordState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState: NarrationState
        get() = throw IllegalStateException("Tried to disable a paused re-recording")
}