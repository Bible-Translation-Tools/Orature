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

class NarrationStateMachine(
    val total: List<AudioMarker>
) {
    private val logger = LoggerFactory.getLogger(NarrationStateMachine::class.java)


    /*
        have this class manage the states for verses (verseContext) and the states for narration (globalContext)
        Each change in the global context causes the verseContext to change entirely.

        Before, the teleprompter state was managing the verse context, and I added an entirely separate machine to
        manage the global context. Then I was having verse nodes listen to changes in the global context and
        make adjustments accordingly.

        Now I want change in the global context to explicitly create the correct verseContext for that particular
        global state.

        For things like the menu options in the header, have different buttons based on the global state.
     */


    private var verseContexts: MutableList<VerseStateContext>

    init {
        verseContexts = total.map { VerseStateContext() }.toMutableList()
    }

    fun initialize(active: List<Boolean>) {
        verseContexts = total.map { VerseStateContext() }.toMutableList()
        active.forEachIndexed { index, hasRecording ->
            verseContexts[index].state = if (hasRecording) RecordAgainState else RecordDisabledState
        }
        verseContexts.firstOrNull { it.state.type == VerseItemState.RECORD_DISABLED }?.state = RecordState
    }
    
    fun transition(request: VerseStateTransition, requestIndex: Int): List<VerseItemState> {
        try {
            when (request) {
                VerseStateTransition.RECORD -> RecordVerseAction.apply(verseContexts, requestIndex)
                VerseStateTransition.PAUSE_RECORDING -> PauseVerseRecordingAction.apply(verseContexts, requestIndex)
                VerseStateTransition.RESUME_RECORDING -> ResumeVerseRecordAction.apply(verseContexts, requestIndex)
                VerseStateTransition.NEXT -> NextVerseAction.apply(verseContexts, requestIndex)
                VerseStateTransition.RECORD_AGAIN -> {
                    if (verseContexts.any { it.state.type == VerseItemState.RECORDING_PAUSED }) {
                        completePausedRecording()
                    }
                    RecordVerseAgainAction.apply(verseContexts, requestIndex)
                }

                VerseStateTransition.PAUSE_RECORD_AGAIN -> PauseRecordVerseAgainAction.apply(
                    verseContexts,
                    requestIndex
                )

                VerseStateTransition.RESUME_RECORD_AGAIN -> ResumeRecordVerseAgainAction.apply(
                    verseContexts,
                    requestIndex
                )

                VerseStateTransition.SAVE -> SaveVerseRecordingAction.apply(verseContexts, requestIndex)
            }
        } catch (e: java.lang.IllegalStateException) {
            logger.error("Error in state transition for requestIndex: $requestIndex, action $request", e)
            throw e
        }
        return verseContexts.map { it.state.type }
    }

    /**
     * If a record again action happens before the chapter is completed, the last recording will be paused. In order to
     * be able to resume correctly after the re-record is finished, we need to first "finish" this verse by moving its
     * state to re-record and enabling recording of the next verse.
     */
    private fun completePausedRecording() {
        val pausedRecordingIndex =
            verseContexts.indexOfFirst { it.state.type == VerseItemState.RECORDING_PAUSED }
        if (pausedRecordingIndex < 0) return

        verseContexts[pausedRecordingIndex].changeState(VerseItemState.RECORD_AGAIN)
        if (pausedRecordingIndex != verseContexts.lastIndex) {
            verseContexts[pausedRecordingIndex + 1].changeState(VerseItemState.RECORD)
        }
    }
}