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

enum class VerseItemState {
    BEGIN_RECORDING,
    RECORD,
    RECORDING_PAUSED,
    RECORD_DISABLED,
    RECORD_ACTIVE,
    RECORD_AGAIN,
    RECORD_AGAIN_ACTIVE,
    RECORD_AGAIN_PAUSED,
    RECORD_AGAIN_DISABLED,
    PLAYING,
    PLAYING_DISABLED,
}

interface VerseState {
    val type: VerseItemState
    val validStateTransitions: Set<VerseItemState>
    val disabledState: VerseState

    fun changeState(request: VerseItemState): VerseState
}

object BeginRecordingState : VerseState {
    override val type = VerseItemState.BEGIN_RECORDING

    override val validStateTransitions = setOf(
        VerseItemState.RECORD,
        VerseItemState.RECORD_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD -> RecordState
            VerseItemState.RECORD_DISABLED -> RecordDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordState : VerseState {
    override val type = VerseItemState.RECORD

    override val validStateTransitions = setOf(
        VerseItemState.RECORD,
        VerseItemState.RECORD_ACTIVE,
        VerseItemState.RECORD_DISABLED,
        VerseItemState.PLAYING,
        VerseItemState.PLAYING_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD -> RecordState
            VerseItemState.RECORD_ACTIVE -> RecordActiveState
            VerseItemState.RECORD_DISABLED -> RecordDisabledState
            VerseItemState.PLAYING -> PlayingState
            VerseItemState.PLAYING_DISABLED -> PlayingDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordDisabledState : VerseState {
    override val type = VerseItemState.RECORD_DISABLED

    override val validStateTransitions = setOf(
        VerseItemState.RECORD,
        VerseItemState.RECORD_ACTIVE,
        VerseItemState.PLAYING_DISABLED
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD -> RecordState
            VerseItemState.RECORD_ACTIVE -> RecordActiveState
            VerseItemState.PLAYING_DISABLED -> PlayingDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordActiveState : VerseState {
    override val type = VerseItemState.RECORD_ACTIVE

    override val validStateTransitions = setOf(
        VerseItemState.RECORDING_PAUSED,
        VerseItemState.RECORD_AGAIN,
        VerseItemState.RECORD_AGAIN_DISABLED
    )

    override val disabledState: VerseState
        get() = throw IllegalStateException("Tried to disable an active recording")

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            // NarrationTextItemState.RECORD_ACTIVE -> RecordActiveState
            VerseItemState.RECORDING_PAUSED -> RecordPausedState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            VerseItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordPausedState : VerseState {
    override val type = VerseItemState.RECORDING_PAUSED

    override val validStateTransitions = setOf(
        VerseItemState.RECORD_ACTIVE,
        VerseItemState.RECORD_AGAIN,
        VerseItemState.RECORD_AGAIN_DISABLED,
        VerseItemState.PLAYING,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD_ACTIVE -> RecordActiveState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            VerseItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            VerseItemState.PLAYING -> PlayingState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}


object PlayingState : VerseState {
    override val type = VerseItemState.PLAYING

    override val validStateTransitions = setOf(
        VerseItemState.RECORDING_PAUSED,
        VerseItemState.RECORD,
        VerseItemState.RECORD_AGAIN,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORDING_PAUSED -> RecordPausedState
            VerseItemState.RECORD -> RecordState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}


object PlayingDisabledState : VerseState {
    override val type = VerseItemState.PLAYING_DISABLED

    override val validStateTransitions = setOf(
        VerseItemState.RECORD,
        VerseItemState.RECORD_AGAIN,
        VerseItemState.RECORD_DISABLED,
        VerseItemState.RECORD_AGAIN_DISABLED,
    )

    override val disabledState = RecordDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: ${type} tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD -> RecordState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            VerseItemState.RECORD_DISABLED -> RecordDisabledState
            VerseItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: ${type} tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainState : VerseState {
    override val type = VerseItemState.RECORD_AGAIN

    override val validStateTransitions = setOf(
        VerseItemState.RECORD_AGAIN_ACTIVE,
        VerseItemState.RECORD_AGAIN_DISABLED,
        VerseItemState.PLAYING,
        VerseItemState.PLAYING_DISABLED
    )

    override val disabledState = RecordAgainDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD_AGAIN_ACTIVE -> RecordAgainActiveState
            VerseItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            VerseItemState.PLAYING -> PlayingState
            VerseItemState.PLAYING_DISABLED -> PlayingDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainDisabledState : VerseState {
    override val type = VerseItemState.RECORD_AGAIN_DISABLED

    override val validStateTransitions = setOf(
        VerseItemState.RECORD_AGAIN,
        VerseItemState.PLAYING_DISABLED
    )

    override val disabledState = RecordAgainDisabledState

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            VerseItemState.PLAYING_DISABLED -> PlayingDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }
}

object RecordAgainActiveState : VerseState {
    override val type = VerseItemState.RECORD_AGAIN_ACTIVE

    override val validStateTransitions = setOf(
        VerseItemState.RECORD_AGAIN_PAUSED,
        VerseItemState.RECORD_AGAIN
    )

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD_AGAIN_PAUSED -> RecordAgainPausedState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState = RecordAgainDisabledState
}

object RecordAgainPausedState : VerseState {
    override val type = VerseItemState.RECORD_AGAIN_PAUSED

    override val validStateTransitions = setOf(
        VerseItemState.RECORD_AGAIN_ACTIVE,
        VerseItemState.RECORD_AGAIN,
        VerseItemState.RECORD_AGAIN_DISABLED
    )

    override fun changeState(request: VerseItemState): VerseState {
        if (request !in validStateTransitions) {
            throw IllegalStateException("State: $type tried to transition to state: $request")
        }

        return when (request) {
            VerseItemState.RECORD_AGAIN_ACTIVE -> RecordAgainActiveState
            VerseItemState.RECORD_AGAIN -> RecordAgainState
            VerseItemState.RECORD_AGAIN_DISABLED -> RecordAgainDisabledState
            else -> {
                throw IllegalStateException("State: $type tried to transition to state: $request")
            }
        }
    }

    override val disabledState: VerseState
        get() = throw IllegalStateException("Tried to disable a paused re-recording")
}