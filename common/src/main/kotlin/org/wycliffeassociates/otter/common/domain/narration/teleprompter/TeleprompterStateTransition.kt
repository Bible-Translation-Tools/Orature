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


object RecordVerseAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        for (i in 0 until index) {
            if (i != index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)

        if (index < contexts.lastIndex) {
            var i = index + 1

            while (i < contexts.size) {
                // enable the next unrecorded node
                if (contexts[i].state.type == TeleprompterItemState.RECORD_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD)
                    break
                }
                contexts[i].disable()
                i++
            }

            // disable the remaining nodes
            (i until contexts.size).forEach {
                contexts[it].disable()
            }
        }
    }
}

object PauseVerseRecordingAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == TeleprompterItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD_AGAIN)
                }
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORDING_PAUSED)
    }
}

object ResumeVerseRecordAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (0 != index) {
            for (i in 0 until index) {
                contexts[i].disable()
            }
        }

        contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)

        if (index < contexts.lastIndex) {
            var i = index + 1

            while (i < contexts.size) {
                // enable the next unrecorded node
                if (contexts[i].state.type == TeleprompterItemState.RECORD_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD)
                    break
                }
                contexts[i].disable()
                i++
            }

            // disable the remaining nodes
            (i until contexts.size).forEach {
                contexts[it].disable()
            }
        }
    }
}

object NextVerseAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, currentIndex: Int) {
        val wasActive = contexts[currentIndex].state.type == TeleprompterItemState.RECORD_ACTIVE

        if (wasActive) {
            contexts[currentIndex].changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
            contexts.firstOrNull { it.state.type == TeleprompterItemState.RECORD_DISABLED }?.changeState(TeleprompterItemState.RECORD_ACTIVE)
        } else {
            contexts[currentIndex].changeState(TeleprompterItemState.RECORD_AGAIN)
            contexts.firstOrNull { it.state.type == TeleprompterItemState.RECORD_DISABLED }?.changeState(TeleprompterItemState.RECORD)
        }
    }
}

object RecordVerseAgainAction {
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

object PauseRecordVerseAgainAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN_PAUSED)
    }
}

object ResumeRecordVerseAgainAction {
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

object SaveVerseRecordingAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        if (index != 0) {
            for (i in 0 until index) {
                contexts[i].restore()
                if (contexts[i].state.type == TeleprompterItemState.RECORD_AGAIN_DISABLED) {
                    contexts[i].changeState(TeleprompterItemState.RECORD_AGAIN)
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


object PlayVerseAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        val isVerseBeingRecorded = contexts[index].state == RecordPausedState

        if (isVerseBeingRecorded) {
            contexts[index].changeState(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED)

            contexts.forEachIndexed { i, verseContext ->
                if (i != index) {
                    contexts[i].disable()
                }
            }
        } else {
            contexts[index].changeState(TeleprompterItemState.PLAYING)

            contexts.forEachIndexed { i, verseContext ->
                if (i != index) {
                    contexts[i].disable()
                }
            }
        }
    }
}


object PauseVersePlaybackAction {
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        if (index !in contexts.indices) return

        val isVerseBeingRecorded = contexts[index].state == PlayingWhileRecordingPausedState

        if (isVerseBeingRecorded) {
            contexts[index].changeState(TeleprompterItemState.RECORDING_PAUSED)
        } else {
            contexts[index].changeState(TeleprompterItemState.RECORD_AGAIN)

            contexts.forEachIndexed { i, verseContext ->
                if (i != index) {
                    contexts[i].restore()
                }
            }
        }
    }
}