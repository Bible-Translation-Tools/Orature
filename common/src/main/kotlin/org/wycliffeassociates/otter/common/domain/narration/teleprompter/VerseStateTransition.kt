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

enum class VerseStateTransition {
    RECORD,
    PAUSE_RECORDING,
    RESUME_RECORDING,
    NEXT,
    RECORD_AGAIN,
    RESUME_RECORD_AGAIN,
    PAUSE_RECORD_AGAIN,
    SAVE,
    PLAY,
    PAUSE_PLAYBACK,
}

object RecordVerseAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        for (i in 0 until index) {
            if (i != index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(VerseItemState.RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(VerseItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseVerseRecordingAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == VerseItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(VerseItemState.RECORD_AGAIN)
                }
            }
        }

        contexts[index].changeState(VerseItemState.RECORDING_PAUSED)
    }
}

object ResumeVerseRecordAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(VerseItemState.RECORD_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            contexts[index + 1].changeState(VerseItemState.RECORD)
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object NextVerseAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        val wasActive = contexts[index - 1].state.type == VerseItemState.RECORD_ACTIVE

        if (wasActive) {
            contexts[index - 1].changeState(VerseItemState.RECORD_AGAIN_DISABLED)
            contexts[index].changeState(VerseItemState.RECORD_ACTIVE)
        } else {
            contexts[index - 1].changeState(VerseItemState.RECORD_AGAIN)
            contexts[index].changeState(VerseItemState.RECORD)
        }
    }
}

object RecordVerseAgainAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(VerseItemState.RECORD_AGAIN_ACTIVE)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object PauseRecordVerseAgainAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        contexts[index].changeState(VerseItemState.RECORD_AGAIN_PAUSED)
    }
}

object ResumeRecordVerseAgainAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(VerseItemState.RECORD_AGAIN_ACTIVE)

        // Make next item available to record
        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].disable()
            }
        }
    }
}

object SaveVerseRecordingAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == VerseItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(VerseItemState.RECORD_AGAIN)
                } else if (contexts[i].state.type == VerseItemState.RECORD_DISABLED) {
                    contexts[i].changeState(VerseItemState.RECORD)
                }
            }
        }

        contexts[index].changeState(VerseItemState.RECORD_AGAIN)

        if (index < contexts.lastIndex) {
            for (i in index + 1..contexts.lastIndex) {
                contexts[i].restore()
            }
        }
    }
}


object PlayVerseAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (contexts[index].state == RecordPausedState) {
            contexts[index].changeState(VerseItemState.PLAYING_WHILE_RECORDING_PAUSED)
        } else {
            contexts[index].changeState(VerseItemState.PLAYING)
        }


        contexts.forEachIndexed { i, verseContext ->
            if (i != index) {
                contexts[i].disable()
            }
        }
    }
}


object PauseVersePlaybackAction {
    fun apply(contexts: MutableList<VerseStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (contexts[index].state == PlayingWhileRecordingPausedState) {
            contexts[index].changeState(VerseItemState.RECORDING_PAUSED)
        } else {
            contexts[index].changeState(VerseItemState.RECORD_AGAIN)
        }

        contexts.forEachIndexed { i, verseContext ->
            if (i != index) {
                contexts[i].restore()
            }
        }
    }
}

