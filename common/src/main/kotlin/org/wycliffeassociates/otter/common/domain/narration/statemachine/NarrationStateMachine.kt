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
package org.wycliffeassociates.otter.common.domain.narration.statemachine

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
    private var narrationContext: NarrationState = IdleEmptyState

    init {
        currentState.connect()
    }

    fun getNarrationContext(): NarrationStateType {
        return narrationContext.type
    }

    fun getVerseItemStates(): List<NarratableItem> {
        return getNarratableItemsList(verseContexts)
    }

    private fun updateNarrationContext(newContext: NarrationState) {
        narrationContext = newContext
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

        updateNarrationContext(newGlobalContext)
    }


    fun transition(request: NarrationStateTransition, requestIndex: Int? = null): List<NarratableItem> {
        try {
            val verseIndex = requestIndex ?: -1
            val newGlobalContext = when (request) {

                NarrationStateTransition.RECORD -> RecordAction.apply(narrationContext, verseContexts, verseIndex)
                NarrationStateTransition.PAUSE_RECORDING -> PauseRecordingAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.RESUME_RECORDING -> ResumeRecordAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.NEXT -> NextAction.apply(narrationContext, verseContexts, verseIndex)


                NarrationStateTransition.RECORD_AGAIN -> {
                    if (verseContexts.any { it.state.type == VerseItemState.RECORDING_PAUSED }) {
                        completePausedRecording()
                    }
                    RecordAgain.apply(narrationContext, verseContexts, verseIndex)
                }


                NarrationStateTransition.PAUSE_RECORD_AGAIN -> PauseRecordAgain.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )


                NarrationStateTransition.RESUME_RECORD_AGAIN -> ResumeRecordAgain.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )


                NarrationStateTransition.SAVE -> SaveAction.apply(narrationContext, verseContexts, verseIndex)

                NarrationStateTransition.PLAY_AUDIO -> PlayAction.apply(narrationContext, verseContexts, verseIndex)

                NarrationStateTransition.PAUSE_AUDIO_PLAYBACK -> PausePlaybackAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.PAUSE_PLAYBACK_WHILE_MODIFYING_AUDIO -> PausePlaybackWhileModifyingAudioAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.SAVE_FINISHED -> SaveFinished.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.MOVING_MARKER -> MovingMarkerAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.PLACE_MARKER -> PlaceMarkerAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )

                NarrationStateTransition.PLACE_MARKER_WHILE_MODIFYING_AUDIO -> PlaceMarkerWhileModifyingAudioAction.apply(
                    narrationContext,
                    verseContexts,
                    verseIndex
                )
            }

            updateNarrationContext(newGlobalContext)
        } catch (e: java.lang.IllegalStateException) {
            logger.error("Error in state transition for requestIndex: $requestIndex, action $request", e)
            throw e
        }
        return getNarratableItemsList(verseContexts)
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

    private fun getNarratableItemsList(verseContexts: List<VerseStateContext>): List<NarratableItem> {

        val isRecording = narrationContext.type == NarrationStateType.RECORDING
        val isRecordingPaused = narrationContext.type == NarrationStateType.RECORDING_PAUSED
        val isRecordingAgain = narrationContext.type == NarrationStateType.RECORDING_AGAIN
        val isRecordAgainPaused = narrationContext.type == NarrationStateType.RECORDING_AGAIN_PAUSED
        val isPlaying = narrationContext.type == NarrationStateType.PLAYING
        val isModifyingAudio = narrationContext.type == NarrationStateType.MODIFYING_AUDIO_FILE


        return verseContexts.map {
            val isAnotherVerseRecordingPaused = isRecordingPaused && it.state.type != VerseItemState.RECORDING_PAUSED

            val hasRecording = it.state.type == VerseItemState.RECORD_AGAIN_PAUSED
                    || it.state.type == VerseItemState.RECORDING_PAUSED || it.state.type == VerseItemState.RECORD_AGAIN

            val isVerseRecordingPaused = it.state.type == VerseItemState.RECORDING_PAUSED


            val isPlayOptionEnabled = hasRecording
                    && !isRecording
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying
                    && !isAnotherVerseRecordingPaused

            val isEditVerseOptionEnabled = hasRecording
                    && !isRecording
                    && !isRecordingPaused
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying
                    && !isModifyingAudio

            val isRecordAgainOptionEnabled = hasRecording
                    && !isRecording
                    && !isRecordingAgain
                    && !isRecordAgainPaused
                    && !isPlaying
                    && !isVerseRecordingPaused

            NarratableItem(it.state.type, isPlayOptionEnabled, isEditVerseOptionEnabled, isRecordAgainOptionEnabled)
        }
    }

}