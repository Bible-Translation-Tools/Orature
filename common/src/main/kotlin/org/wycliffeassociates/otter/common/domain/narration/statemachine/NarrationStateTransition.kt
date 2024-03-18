package org.wycliffeassociates.otter.common.domain.narration.statemachine


enum class NarrationStateTransition {
    RECORD,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    NEXT,
    RECORD_AGAIN,
    RESUME_RECORD_AGAIN,
    PAUSE_RECORD_AGAIN,
    SAVE,
    SAVE_FINISHED,
    PLAY_AUDIO,
    PAUSE_AUDIO_PLAYBACK,
    PAUSE_PLAYBACK_WHILE_MODIFYING_AUDIO,
    MOVING_MARKER,
    PLACE_MARKER,
    PLACE_MARKER_WHILE_MODIFYING_AUDIO,
}


object RecordAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        RecordVerseAction.apply(verseContexts, index)
        return globalContext.changeState(NarrationStateType.RECORDING)

    }
}


object PauseRecordingAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        PauseVerseRecordingAction.apply(verseContexts, index)
        return globalContext.changeState(NarrationStateType.RECORDING_PAUSED)

    }
}


object ResumeRecordAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        ResumeVerseRecordAction.apply(verseContexts, index)
        return globalContext.changeState(NarrationStateType.RECORDING)

    }
}


object NextAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        NextVerseAction.apply(verseContexts, index)

        val isRecording = verseContexts.any { it.state == RecordActiveState || it.state == RecordAgainActiveState }

        return if (isRecording) {
            globalContext.changeState(NarrationStateType.RECORDING)
        } else {
            globalContext.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        }

    }

}


object RecordAgain {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        RecordVerseAgainAction.apply(verseContexts, index)

        return globalContext.changeState(NarrationStateType.RECORDING_AGAIN)
    }

}


object PauseRecordAgain {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        PauseRecordVerseAgainAction.apply(verseContexts, index)

        return globalContext.changeState(NarrationStateType.RECORDING_AGAIN_PAUSED)
    }
}


object ResumeRecordAgain {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        ResumeRecordVerseAgainAction.apply(verseContexts, index)

        return globalContext.changeState(NarrationStateType.RECORDING_AGAIN)
    }
}


object SaveAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        if (index >= 0) {
            SaveVerseRecordingAction.apply(verseContexts, index)
        }

        val allVersesRecorded = verseContexts.all { it.state == RecordAgainState }

        return if (allVersesRecorded) {
            globalContext.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
        } else {
            globalContext.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        }

    }
}


object SaveFinished {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        val allVersesRecorded =
            verseContexts.all {
                it.state == RecordAgainState
                        || it.state == RecordAgainActiveState
                        || it.state == RecordAgainPausedState
            }

        val isRecordingAgain = verseContexts.any { it.state == RecordAgainActiveState }

        val isRecordAgainPaused = verseContexts.any { it.state == RecordAgainPausedState }

        return if (allVersesRecorded && !isRecordingAgain && !isRecordAgainPaused) {
            globalContext.changeState(NarrationStateType.IDLE_FINISHED)
        } else if (isRecordingAgain) {
            globalContext.changeState(NarrationStateType.RECORDING_AGAIN)
        } else {
            globalContext.changeState(NarrationStateType.RECORDING_AGAIN_PAUSED)
        }

    }
}

object PlayAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        // Set the individual verse to playing if given a valid index.
        if (index >= 0) {
            PlayVerseAction.apply(verseContexts, index)
        } else {

            val recordingPausedVerse = verseContexts.firstOrNull { it.state.type == VerseItemState.RECORDING_PAUSED }
            if (recordingPausedVerse != null) {
                throw IllegalStateException(
                    "Tried to play all verses while verse $recordingPausedVerse " +
                            "is recording paused"
                )
            }


            verseContexts.forEach {
                it.disable()
            }
        }

        return globalContext.changeState(NarrationStateType.PLAYING)
    }
}


object PausePlaybackAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        val wasRecordingPaused = verseContexts.any { it.state == PlayingWhileRecordingPausedState }
        val allVersesRecorded =
            !verseContexts.any { it.state == RecordDisabledState }
                    && verseContexts.last().state.type != VerseItemState.PLAYING_WHILE_RECORDING_PAUSED

        val newGlobalStateRequest = if (wasRecordingPaused) {
            NarrationStateType.RECORDING_PAUSED
        } else if (allVersesRecorded) {
            NarrationStateType.IDLE_FINISHED
        } else {
            NarrationStateType.IDLE_IN_PROGRESS
        }


        val playingVerse = verseContexts.indexOfFirst {
            it.state.type == VerseItemState.PLAYING
                    || it.state.type == VerseItemState.PLAYING_WHILE_RECORDING_PAUSED
        }

        if (playingVerse >= 0) {
            PauseVersePlaybackAction.apply(verseContexts, playingVerse)
        } else {
            verseContexts.forEach {
                it.restore()
            }
        }

        return globalContext.changeState(newGlobalStateRequest)
    }
}


object PausePlaybackWhileModifyingAudioAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        val playingVerse = verseContexts.indexOfFirst {
            it.state.type == VerseItemState.PLAYING
                    || it.state.type == VerseItemState.PLAYING_WHILE_RECORDING_PAUSED
        }

        if (playingVerse >= 0) {
            PauseVersePlaybackAction.apply(verseContexts, playingVerse)
        } else {
            verseContexts.forEach {
                it.restore()
            }
        }

        return globalContext.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
    }
}


object MovingMarkerAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {


        // TODO note: I should transition the individual marker to MOVING

        return globalContext.changeState(NarrationStateType.MOVING_MARKER)
    }

}


object PlaceMarkerAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        // TODO note: I should transition the individual marker back to not moving

        val allVersesRecorded =
            !verseContexts.any {
                it.state.type == VerseItemState.RECORD_DISABLED || it.state.type == VerseItemState.RECORD
            }

        return if (allVersesRecorded) {
            globalContext.changeState(NarrationStateType.IDLE_FINISHED)
        } else {
            globalContext.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        }
    }
}


object PlaceMarkerWhileModifyingAudioAction {
    fun apply(
        globalContext: NarrationState,
        verseContexts: MutableList<VerseStateContext>,
        index: Int
    ): NarrationState {

        // TODO note: I should transition the individual marker back to not moving

        return globalContext.changeState(NarrationStateType.MODIFYING_AUDIO_FILE)
    }
}











