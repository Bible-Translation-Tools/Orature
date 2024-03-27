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

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.domain.narration.teleprompter.*
import java.lang.IllegalStateException

class TeleprompterStateMachineTest {

    fun mockAudioMarker(): AudioMarker {
        return mockk<AudioMarker> {}
    }

    fun makeAudioMarkerLists(size: Int): List<AudioMarker> {
        return List(size) { mockAudioMarker() }
    }


    @Test
    fun `initialize with no verses recorded`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an idle empty state
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        val contexts = TeleprompterStateMachine.getVerseItemStates()

        for (i in audioMarkers.indices) {
            if (i == 0) {
                Assert.assertEquals(TeleprompterItemState.RECORD, contexts[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, contexts[i].verseState)
            }
        }
    }


    @Test
    fun `initialize with some verses recorded`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = MutableList(10) { false }
        val recordedVerses = 5
        for (i in 0 until recordedVerses) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an idle empty state
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val contexts = TeleprompterStateMachine.getVerseItemStates()

        for (i in audioMarkers.indices) {
            if (i < recordedVerses) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, contexts[i].verseState)
            } else if (i == recordedVerses) {
                Assert.assertEquals(TeleprompterItemState.RECORD, contexts[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, contexts[i].verseState)
            }
        }
    }


    @Test
    fun `initialize with all verses recorded`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { true }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an IDLE_FINISHED state
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        val contexts = TeleprompterStateMachine.getVerseItemStates()

        for (i in audioMarkers.indices) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, contexts[i].verseState)
        }
    }


    @Test
    fun `initialize to PLAY_AUDIO with some verses recorded`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = MutableList(10) { false }
        val versesToRecord = 5
        for (i in 0 until versesToRecord) {
            activeVerses[i] = true
        }
        val verseToPlay = 3

        TeleprompterStateMachine.initialize(activeVerses)

        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val contexts = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseToPlay)
        Assert.assertEquals(NarrationStateType.PLAYING, TeleprompterStateMachine.getNarrationContext())

        for (i in audioMarkers.indices) {
            if (i == verseToPlay) {
                Assert.assertEquals(TeleprompterItemState.PLAYING, contexts[i].verseState)
            } else if (i < versesToRecord) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, contexts[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, contexts[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD to RECORD with multiple items in context and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an idle empty state
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        val index = 0
        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        // Verifies that the state at index is RECORD_ACTIVE, and that the rest are RECORD_DISABLED
        for (i in audioMarkers.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }

        // Verifies that the narration context is in a recording state
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())
    }


    @Test
    fun `transition from RECORD to RECORDING_PAUSED and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an idle empty state
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())


        val index = 0
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        // Verifies that the narration context is in a recording state
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Verify that newContext[index] is in the RECORD_ACTIVE state.
        Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[index].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }

        // Verifies that the narration context is in a recording paused state
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
    }


    @Test
    fun `transition from RECORD_AGAIN then RECORD_AGAIN_PAUSED with first two active and recording second verse`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = MutableList(10) { false }
        activeVerses[0] = true
        activeVerses[1] = true

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an IDLE_IN_PROGRESS state
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val index = 1
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD_AGAIN, index)

        // Verifies that the narration context is in a recording again state
        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN, TeleprompterStateMachine.getNarrationContext())

        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[index].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORD_AGAIN, index)

        // Verifies that the narration context is in a recording again paused state
        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN_PAUSED, TeleprompterStateMachine.getNarrationContext())

        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[index].verseState)

        for (i in newContext.indices) {
            if (i < index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i].verseState)
            } else if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[i].verseState)
            } else if (i > index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RESUME_RECORDING with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        val index = 0

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        // Start recording
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        // Pause recording
        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Resume recording
        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RESUME_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        val index = 0

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        // Start recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Resume recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RESUME_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Next
        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i].verseState)
            } else if (i == index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, with none previously active and resuming recording of verse 3`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())


        // Records verse 1
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, 0)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, 0)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, 0)

        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        // Records verse 2
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, 1)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, 1)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, 1)

        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        // Starts recording for verse 3
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, 2)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, 2)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RESUME_RECORDING, 2)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        for (i in newContext.indices) {
            if (i < 2) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i].verseState)
            } else if (i == 2) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        val index = 0

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        // Start recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Next
        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
            } else if (i == index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        // Verifies that the narration context is in an IDLE_EMPTY state
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        val index = 0

        // Start recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        // Verifies that the narration context is in a recording
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        // Verifies that the narration context is in a recording paused state
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Next
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, index)

        // Verifies that the narration context is in an IDLE_IN_PROGRESS state
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        // Verify that newContext[index] is in the RECORD_AGAIN state
        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[index].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD_AGAIN, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, then PAUSE_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        val index = 0

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        // Start recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Next
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, index)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD_AGAIN, index)

        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN, TeleprompterStateMachine.getNarrationContext())

        // Verify that newContext[index] is in teh RECORD_AGAIN_ACTIVE state
        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[index].verseState)

        // Pause recording
        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORD_AGAIN, index)

        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN_PAUSED, TeleprompterStateMachine.getNarrationContext())

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, PAUSE_RECORD_AGAIN, RESUME_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        val index = 0

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())


        // Start recording
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, index)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, index)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Next
        TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, index)

        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD_AGAIN, index)

        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN, TeleprompterStateMachine.getNarrationContext())

        // Pause recording
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORD_AGAIN, index)

        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Resume recording
        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RESUME_RECORD_AGAIN, index)

        Assert.assertEquals(NarrationStateType.RECORDING_AGAIN, TeleprompterStateMachine.getNarrationContext())



        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT, repeated until end of chapter, then SAVE with none previously active`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }

        TeleprompterStateMachine.initialize(activeVerses)

        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        // Puts all verses up to the last verse in a RECORD_AGAIN state
        var newContext: List<NarratableItem>
        for (i in 0 until numMarkers - 1) {
            // Start recording
            TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, i)

            Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

            // Pause recording
            TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, i)

            Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

            // Next
            newContext = TeleprompterStateMachine.transition(NarrationStateTransition.NEXT, i)

            Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())
        }

        // Records the last verse
        TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, numMarkers - 1)

        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())

        // Pause
        TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, numMarkers - 1)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        // Saves
        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.START_SAVE, numMarkers - 1)

        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())


        for (i in newContext.indices) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
        }
    }


    @Test
    fun `transition from RECORD, RECORD_PAUSED, then PLAY with 5 verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 5

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val verseIndexToRecord = versesPreviouslyRecorded
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.PLAYING, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            if (i < verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i].verseState)
            } else if (i == verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }

    @Test
    fun `transition from RECORDING, RECORDING_PAUSED, PLAY, PAUSE_AUDIO_PLAYBACK with 5 verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 5

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val verseIndexToRecord = versesPreviouslyRecorded
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.PLAYING, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(
            TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED,
            newContext[verseIndexToRecord].verseState
        )

        newContext =
            TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_AUDIO_PLAYBACK, verseIndexToRecord)

        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            if (i < verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
            } else if (i == verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORDING, RECORDING_PAUSED, PLAY (non-recording verse)`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val verseToPlay = 3

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        val verseIndexToRecord = 0
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        try {
            newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseToPlay)
            Assert.fail(
                "Error: expected an exception when playing verse $verseToPlay while recording verse " +
                        "$verseIndexToRecord."
            )

        } catch (e: IllegalStateException) {
            // Success: exception was expected
        }
    }


    @Test
    fun `transition from RECORDING, RECORDING_PAUSED, resume RECORDING, RECORDING_PAUSED, PLAY (non-recording verse)`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val verseToPlay = 0

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.NOT_STARTED, TeleprompterStateMachine.getNarrationContext())

        val verseIndexToRecord = 0
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[verseIndexToRecord].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO, verseToPlay)

        for (i in 0 until activeVerses.size) {
            if (i < verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i].verseState)
            } else if (i == verseIndexToRecord) {
                Assert.assertEquals(TeleprompterItemState.PLAYING_WHILE_RECORDING_PAUSED, newContext[i].verseState)
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i].verseState)
            }
        }
    }


    @Test
    fun `transition from RECORDING_PAUSED to PLAY_ALL with 5 verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 5

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.HAS_RECORDINGS, TeleprompterStateMachine.getNarrationContext())

        val verseIndexToRecord = 5
        var newContext = TeleprompterStateMachine.transition(NarrationStateTransition.RECORD, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING, TeleprompterStateMachine.getNarrationContext())


        newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_RECORDING, verseIndexToRecord)
        Assert.assertEquals(NarrationStateType.RECORDING_PAUSED, TeleprompterStateMachine.getNarrationContext())
        Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[verseIndexToRecord].verseState)

        try {
            newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO)
            Assert.fail(
                "Error: expected an exception when playing all verses while recording verse " +
                        "$verseIndexToRecord."
            )

        } catch (e: IllegalStateException) {
            // Success: exception was expected
        }
    }


    @Test
    fun `transition from MODIFYING_AUDIO_FILE, PLAYING, to MODIFYING_AUDIO_FILE with all verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 10

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.START_SAVE)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO)
        Assert.assertEquals(NarrationStateType.PLAYING, TeleprompterStateMachine.getNarrationContext())

        val newContext =
            TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_PLAYBACK_WHILE_MODIFYING_AUDIO)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
        }
    }


    @Test
    fun `transition from MODIFYING_AUDIO_FILE, PLAYING, to IDLE_FINISHED with all verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 10

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.START_SAVE)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.PLAY_AUDIO)
        Assert.assertEquals(NarrationStateType.PLAYING, TeleprompterStateMachine.getNarrationContext())

        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PAUSE_AUDIO_PLAYBACK)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
        }
    }


    @Test
    fun `transition from MODIFYING_AUDIO_FILE, MOVING_MARKER, to MODIFYING_AUDIO_FILE with all verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 10

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.START_SAVE)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.MOVING_MARKER)
        Assert.assertEquals(NarrationStateType.MOVING_MARKER, TeleprompterStateMachine.getNarrationContext())

        val newContext =
            TeleprompterStateMachine.transition(NarrationStateTransition.PLACE_MARKER_WHILE_MODIFYING_AUDIO)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
        }
    }


    @Test
    fun `transition from MODIFYING_AUDIO_FILE, MOVING_MARKER, to IDLE_IN_PROGRESS with all verses previously recorded`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val TeleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }.toMutableList()
        val versesPreviouslyRecorded = 10

        for (i in 0 until versesPreviouslyRecorded) {
            activeVerses[i] = true
        }

        TeleprompterStateMachine.initialize(activeVerses)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.START_SAVE)
        Assert.assertEquals(NarrationStateType.MODIFYING_AUDIO_FILE, TeleprompterStateMachine.getNarrationContext())

        TeleprompterStateMachine.transition(NarrationStateTransition.MOVING_MARKER)
        Assert.assertEquals(NarrationStateType.MOVING_MARKER, TeleprompterStateMachine.getNarrationContext())

        val newContext = TeleprompterStateMachine.transition(NarrationStateTransition.PLACE_MARKER)
        Assert.assertEquals(NarrationStateType.FINISHED, TeleprompterStateMachine.getNarrationContext())

        for (i in 0 until activeVerses.size) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i].verseState)
        }
    }


}




