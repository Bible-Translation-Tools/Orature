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

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.observables.ConnectableObservable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.audio.AudioMarker

class NarrationStateMachine(
    val total: List<AudioMarker>
) {
    private val logger = LoggerFactory.getLogger(NarrationStateMachine::class.java)

    private lateinit var currentStateEmitter: ObservableEmitter<NarrationState>

    val currentState: ConnectableObservable<NarrationState> = Observable.create { emitter ->
        currentStateEmitter = emitter
        emitter.onNext(IdleEmptyState)
    }.publish()

    private var verseContexts: MutableList<VerseStateContext>
    private var globalContext: NarrationState = IdleEmptyState

    init {
        currentState.connect()
    }

    fun getGlobalContext(): NarrationState {
        return globalContext
    }

    fun getVerseItemStates(): List<VerseItemState> {
        return verseContexts.map { it.state.type }
    }

    private fun updateGlobalContext(newContext: NarrationState) {
        globalContext = newContext
        currentStateEmitter.onNext(newContext)
    }

    init {
        verseContexts = total.map { VerseStateContext() }.toMutableList()
    }

    fun initialize(active: List<Boolean>) {
        verseContexts = total.map { VerseStateContext() }.toMutableList()

        var hasAllItemsRecorded = true
        var hasNoItemsRecorded = true

        active.forEachIndexed { index, hasRecording ->
            verseContexts[index].state = if (hasRecording) {
                hasNoItemsRecorded = false
                RecordAgainState
            } else {
                hasAllItemsRecorded = false
                RecordDisabledState
            }
        }
        verseContexts.firstOrNull { it.state.type == VerseItemState.RECORD_DISABLED }?.state = RecordState

        val newGlobalContext = if (hasAllItemsRecorded) {
            IdleFinishedState
        } else if (hasNoItemsRecorded) {
            IdleEmptyState
        } else {
            IdleInProgressState
        }

        updateGlobalContext(newGlobalContext)
    }


    fun transition(request: NarrationStateTransition, requestIndex: Int): List<VerseItemState> {
        try {
            val newGlobalContext = when (request) {

                NarrationStateTransition.RECORD -> RecordAction.apply(globalContext, verseContexts, requestIndex)
                NarrationStateTransition.PAUSE_RECORDING -> PauseRecordingAction.apply(
                    globalContext,
                    verseContexts,
                    requestIndex
                )

                NarrationStateTransition.RESUME_RECORDING -> ResumeRecordAction.apply(
                    globalContext,
                    verseContexts,
                    requestIndex
                )

                NarrationStateTransition.NEXT -> NextAction.apply(globalContext, verseContexts, requestIndex)


                NarrationStateTransition.RECORD_AGAIN -> {
                    if (verseContexts.any { it.state.type == VerseItemState.RECORDING_PAUSED }) {
                        completePausedRecording()
                    }
                    RecordAgain.apply(globalContext, verseContexts, requestIndex)
                }


                NarrationStateTransition.PAUSE_RECORD_AGAIN -> PauseRecordAgain.apply(
                    globalContext,
                    verseContexts,
                    requestIndex
                )


                NarrationStateTransition.RESUME_RECORD_AGAIN -> ResumeRecordAgain.apply(
                    globalContext,
                    verseContexts,
                    requestIndex
                )


                NarrationStateTransition.SAVE -> SaveAction.apply(globalContext, verseContexts, requestIndex)

                NarrationStateTransition.PLAY_AUDIO -> PlayAction.apply(globalContext, verseContexts, requestIndex)

                NarrationStateTransition.PAUSE_AUDIO_PLAYBACK -> PausePlaybackAction.apply(
                    globalContext,
                    verseContexts,
                    requestIndex
                )

                NarrationStateTransition.SAVE_FINISHED -> SaveFinished.apply(globalContext, verseContexts, requestIndex)
            }

            updateGlobalContext(newGlobalContext)
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