package org.wycliffeassociates.otter.common.domain.narration.teleprompter

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.AudioMarker

class TeleprompterStateMachine(
    val total: List<AudioMarker>
) {
    private val logger = LoggerFactory.getLogger(TeleprompterStateMachine::class.java)
    private var contexts: MutableList<TeleprompterStateContext>

    init {
        contexts = total.map { TeleprompterStateContext() }.toMutableList()
    }

    fun initialize(active: List<Boolean>) {
        contexts = total.map { TeleprompterStateContext() }.toMutableList()
        active.forEachIndexed { index, hasRecording ->
            contexts[index].state = if (hasRecording) RecordAgainState else RecordDisabledState
        }
        contexts.firstOrNull { it.state.type == TeleprompterItemState.RECORD_DISABLED }?.state = RecordState
    }

    fun transition(request: TeleprompterStateTransition, requestIndex: Int): List<TeleprompterItemState> {
        try {
            when (request) {
                TeleprompterStateTransition.RECORD -> RecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.PAUSE_RECORDING -> PauseRecordingAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.RESUME_RECORDING -> ResumeRecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.NEXT -> NextVerseAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.RECORD_AGAIN -> RecordAgainAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.PAUSE_RECORD_AGAIN -> PauseRecordAgainAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.RESUME_RECORD_AGAIN -> ResumeRecordAgainAction.apply(contexts, requestIndex)
                TeleprompterStateTransition.SAVE -> SaveRecordingAction.apply(contexts, requestIndex)
            }
        } catch (e: java.lang.IllegalStateException) {
            logger.error("Error in state transition for requestIndex: $requestIndex, action $request", e)
            throw e
        }
        return contexts.map { it.state.type }
    }
}