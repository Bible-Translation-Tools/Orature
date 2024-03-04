package org.wycliffeassociates.otter.common.domain.narration.teleprompter


enum class NarrationStateTransition {
    RECORD,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    NEXT,
    RECORD_AGAIN,
    RESUME_RECORD_AGAIN,
    PAUSE_RECORD_AGAIN,
    SAVE,
    UNDO, // TODO
    REDO, // TODO
    RESTART_CHAPTER, // TODO
    EDIT_VERSE, // TODO
    EDIT_CHAPTER, // TODO
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

        // TODO: make sure that this is correct
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

        SaveVerseRecordingAction.apply(verseContexts, index)


        val allVersesRecorded = verseContexts.all { it.state == RecordAgainState }

        return if (allVersesRecorded) {
            globalContext.changeState(NarrationStateType.BOUNCING_AUDIO)
        } else {
            globalContext.changeState(NarrationStateType.IDLE_IN_PROGRESS)
        }

    }
}













