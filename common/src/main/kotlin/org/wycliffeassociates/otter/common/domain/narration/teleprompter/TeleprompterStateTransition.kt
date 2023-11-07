package org.wycliffeassociates.otter.common.domain.narration.teleprompter

enum class TeleprompterStateTransition {
    RECORD,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    NEXT,
    RECORD_AGAIN,
    RESUME_RECORD_AGAIN,
    PAUSE_RECORD_AGAIN,
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
            // NOTE: this should be index + 2, because we are overriding the Record above
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
                // Why does this need a condition and why can't it utilize the restore method?
                if (contexts[i].state.type == TeleprompterItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD_AGAIN)
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
            // This also seems that it should be index + 2
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
            contexts[index - 1].changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
            contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)
        } else {
            // TODO: this needs to reset all other items that had a state of RECORD_AGAIN_DISABLED
            //  to RECORD_AGAIN. Curently this just changes one prior to the new verse.
            contexts[index - 1].changeState(TeleprompterItemState.RECORD_AGAIN)
            contexts[index].changeState(TeleprompterItemState.RECORD)
        }
    }
}

object RecordAgainAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN_ACTIVE)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseRecordAgainAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN_PAUSED)
    }
}

object ResumeRecordAgainAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN_ACTIVE)

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
                if (contexts[i].state.type == TeleprompterItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD_AGAIN)
                } else if (contexts[i].state.type == TeleprompterItemState.RECORD_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD)
                }
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].restore()
            }
        }
    }
}