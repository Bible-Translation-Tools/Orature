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
            contexts[index].state = if (hasRecording) ReRecordState else RecordDisabledState
        }
        contexts.firstOrNull { it.state.type == TeleprompterItemState.RECORD_DISABLED }?.state = RecordState
    }

    fun applyTransition(request: TeleprompterStateTransitions, requestIndex: Int): List<TeleprompterItemState> {
        try {
            when (request) {
                TeleprompterStateTransitions.RECORD -> RecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.PAUSE_RECORDING -> PauseRecordingAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.RESUME_RECORDING -> ResumeRecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.NEXT -> NextVerseAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.RE_RECORD -> ReRecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.PAUSE_RE_RECORD -> PauseReRecordingAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.RESUME_RE_RECORDING -> ResumeReRecordAction.apply(contexts, requestIndex)
                TeleprompterStateTransitions.SAVE -> SaveRecordingAction.apply(contexts, requestIndex)
            }
        } catch (e: java.lang.IllegalStateException) {
            logger.error("Error in state transition for requestIndex: $requestIndex, action $request", e)
            throw e
        }
        return contexts.map { it.state.type }
    }
}