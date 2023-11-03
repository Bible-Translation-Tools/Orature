package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class TeleprompterStateTransitions {
    RECORD,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    NEXT,
    RE_RECORD,
    RESUME_RE_RECORDING,
    PAUSE_RE_RECORD,
    SAVE
}

object RecordAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        for (i in 0 until index) {
            if (i != index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(TeleprompterItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseRecordingAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == TeleprompterItemState.RE_RECORD_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RE_RECORD)
                }
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORDING_PAUSED)
    }
}

object ResumeRecordAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(TeleprompterItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object NextVerseAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        val wasActive = contexts[index - 1].state.type == TeleprompterItemState.RECORD_ACTIVE

        if (wasActive) {
            contexts[index - 1].changeState(TeleprompterItemState.RE_RECORD_DISABLED)
            contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)
        } else {
            contexts[index - 1].changeState(TeleprompterItemState.RE_RECORD)
            contexts[index].changeState(TeleprompterItemState.RECORD)
        }
    }
}

object ReRecordAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RE_RECORD_ACTIVE)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseReRecordingAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        contexts[index].changeState(TeleprompterItemState.RE_RECORDING_PAUSED)
    }
}

object ResumeReRecordAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RE_RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object SaveRecordingAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].restore()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RE_RECORD)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].restore()
            }
        }
    }
}