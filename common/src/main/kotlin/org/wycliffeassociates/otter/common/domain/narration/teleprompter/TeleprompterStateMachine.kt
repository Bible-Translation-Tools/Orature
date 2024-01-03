/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
                TeleprompterStateTransition.RECORD_AGAIN -> {
                    if (contexts.any { it.state.type == TeleprompterItemState.RECORDING_PAUSED }) {
                        completePausedRecording()
                    }
                    RecordAgainAction.apply(contexts, requestIndex)
                }
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

    /**
     * If a record again action happens before the chapter is completed, the last recording will be paused. In order to
     * be able to resume correctly after the re-record is finished, we need to first "finish" this verse by moving its
     * state to re-record and enabling recording of the next verse.
     */
    private fun completePausedRecording() {
        val pausedRecordingIndex = contexts.indexOfFirst { it.state.type == TeleprompterItemState.RECORDING_PAUSED}
        if (pausedRecordingIndex < 0) return

        contexts[pausedRecordingIndex].changeState(TeleprompterItemState.RECORD_AGAIN)
        if (pausedRecordingIndex != contexts.lastIndex) {
            contexts[pausedRecordingIndex + 1].changeState(TeleprompterItemState.RECORD)
        }
    }
}