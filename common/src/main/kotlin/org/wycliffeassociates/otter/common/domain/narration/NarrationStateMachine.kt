package org.wycliffeassociates.otter.common.domain.narration

import org.slf4j.LoggerFactory
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

enum class NarrationStateTransitions {
    RECORD,
    PAUSE_RECORDING,
    NEXT,
    RE_RECORD,
    PAUSE_RE_RECORD,
    SAVE
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
    private val logger = LoggerFactory.getLogger(NarrationStateMachine::class.java)
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
                contexts[1].state = RecordDisabledState
            }
            for (index in contexts.indices) {
                if (index !in recordedIndices) {
                    contexts[index].state = RecordDisabledState
                }
            }
        }
    }

    fun changeState(
        request: NarrationTextItemState,
        requestIndex: Int
    ): List<NarrationTextItemState> {
        if (request !in contexts[requestIndex].state.validStateTransitions) {
            throw IllegalStateException(
                "Could not complete state transition, $request is not a valid transition from ${contexts[requestIndex].state.type}"
            )
        }
        return contexts.mapIndexed { index, context ->
            val orientation = when {
                requestIndex == index -> NarrationStateEventPosition.CURRENT
                requestIndex - 1 == index -> NarrationStateEventPosition.PRECEDING
                requestIndex + 1 == index -> NarrationStateEventPosition.SUCCEEDING
                else -> NarrationStateEventPosition.PERIPHERAL
            }

            context.changeState(request)
            context.state.type
        }
    }

    fun applyTransition(request: NarrationStateTransitions, requestIndex: Int): List<NarrationTextItemState> {
        try {
            when (request) {
                NarrationStateTransitions.RECORD -> RecordAction.apply(contexts, requestIndex)
                NarrationStateTransitions.PAUSE_RECORDING -> PauseRecordingAction.apply(contexts, requestIndex)
                NarrationStateTransitions.NEXT -> NextVerseAction.apply(contexts, requestIndex)
                NarrationStateTransitions.RE_RECORD -> ReRecordAction.apply(contexts, requestIndex)
                NarrationStateTransitions.PAUSE_RE_RECORD -> PauseReRecordingAction.apply(contexts, requestIndex)
                NarrationStateTransitions.SAVE -> SaveRecordingAction.apply(contexts, requestIndex)
            }
        } catch (e: java.lang.IllegalStateException) {
            logger.error("Error in state transition for requestIndex: $requestIndex, action $request", e)
            throw e
        }
        return contexts.map { it.state.type }
    }
}

class NarrationStateContext {
    lateinit var state: NarrationState
        internal set

    private var temporarilyDisabledState: NarrationState? = null

    fun changeState(
        request: NarrationTextItemState
    ) {
        temporarilyDisabledState = null
        state = state.changeState(request)
    }

    fun disable() {
        temporarilyDisabledState = state
        state = state.disabledState
    }

    fun restore() {
        state = temporarilyDisabledState ?: state
        temporarilyDisabledState = null
    }
}

object RecordAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        if (index !in contexts.indices) return

        for (i in 0 until index) {
            if (i != index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(NarrationTextItemState.RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(NarrationTextItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseRecordingAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == NarrationTextItemState.RE_RECORD_DISABLED) {
                    contexts[i].state.type == NarrationTextItemState.RE_RECORD
                }
            }
        }

        contexts[index].changeState(NarrationTextItemState.RECORDING_PAUSED)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(NarrationTextItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].restore()
                if (contexts[i].state.type == NarrationTextItemState.RE_RECORD_DISABLED) {
                    contexts[i].state.type == NarrationTextItemState.RE_RECORD
                }
            }
        }
    }
}

object NextVerseAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        val wasActive = contexts[index - 1].state.type == NarrationTextItemState.RECORD_ACTIVE

        contexts[index - 1].changeState(NarrationTextItemState.RE_RECORD_DISABLED)

        when (wasActive) {
            true -> contexts[index].changeState(NarrationTextItemState.RECORD_ACTIVE)
            false -> contexts[index].changeState(NarrationTextItemState.RECORDING_PAUSED)
        }
    }
}

object ReRecordAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(NarrationTextItemState.RE_RECORD_ACTIVE)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseReRecordingAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        contexts[index].changeState(NarrationTextItemState.RE_RECORDING_PAUSED)
    }
}

object SaveRecordingAction {
    fun apply(contexts: MutableList<NarrationStateContext>, index: Int) {
        if (index !in contexts.indices) return

        for (i in 0 until index) {
            contexts[i].restore()
        }

        contexts[index].changeState(NarrationTextItemState.RE_RECORD)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].restore()
            }
        }
    }
}

interface NarrationState {
    val type: NarrationTextItemState
    val validStateTransitions: Set<NarrationTextItemState>
    val disabledState: NarrationState

    fun changeState(request: NarrationTextItemState): NarrationState
}

object BeginRecordingState : NarrationState {
    override val type = NarrationTextItemState.BEGIN_RECORDING

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORD,
        NarrationTextItemState.RECORD_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState): NarrationState {
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
        NarrationTextItemState.RECORD,
        NarrationTextItemState.RECORD_ACTIVE,
        NarrationTextItemState.RECORD_DISABLED,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
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
        NarrationTextItemState.RECORD,
        NarrationTextItemState.RECORD_ACTIVE
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            NarrationTextItemState.RECORD -> RecordState
            NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordActiveState : NarrationState {
    override val type = NarrationTextItemState.RECORD_ACTIVE

    override val validStateTransitions = setOf(
        NarrationTextItemState.RECORDING_PAUSED,
        NarrationTextItemState.RE_RECORD_DISABLED
    )

    override val disabledState: NarrationState
        get() = throw IllegalStateException("Tried to disable an active recording")

    override fun changeState(request: NarrationTextItemState): NarrationState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            // NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            NarrationTextItemState.RECORDING_PAUSED -> RecordPausedState
            NarrationTextItemState.RE_RECORD_DISABLED -> ReRecordDisabledState
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

    override fun changeState(request: NarrationTextItemState): NarrationState {
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

    override fun changeState(request: NarrationTextItemState): NarrationState {
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

    override fun changeState(request: NarrationTextItemState): NarrationState {
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

    override fun changeState(request: NarrationTextItemState): NarrationState {
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

    override fun changeState(request: NarrationTextItemState): NarrationState {
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