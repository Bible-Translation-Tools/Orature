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

object PauseRecordingAction {
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

object ResumeRecordAction {
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
    fun apply(contexts: MutableList<TeleprompterStateContext>, index: Int) {
        val wasActive = contexts[index - 1].state.type == TeleprompterItemState.RECORD_ACTIVE

        if (wasActive) {
            contexts[index - 1].changeState(TeleprompterItemState.RECORD_AGAIN_DISABLED)
            contexts[index].changeState(TeleprompterItemState.RECORD_ACTIVE)
        } else {
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