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

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.AudioMarker

class NarrationStateMachineTest {

    fun mockAudioMarker(): AudioMarker {
        return mockk<AudioMarker> {}
    }

    fun makeAudioMarkerLists(size: Int): List<AudioMarker> {
        return List(size) { mockAudioMarker() }
    }

    @Test
    fun `transition from RECORD to RECORD with multiple items in context and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0
        val newContext = narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Verifies that the state at index is RECORD_ACTIVE, and that the rest are RECORD_DISABLED
        for (i in audioMarkers.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD to RECORDING_PAUSED and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)


        val index = 0
        var newContext = narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Verify that newContext[index] is in the RECORD_ACTIVE state.
        Assert.assertEquals(VerseItemState.RECORD_ACTIVE, newContext[index])

        newContext = narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORDING_PAUSED, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD_AGAIN then RECORD_AGAIN_PAUSED with first two active and recording second verse`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = MutableList(10) { false }
        activeVerses[0] = true
        activeVerses[1] = true

        narrationStateMachine.initialize(activeVerses)

        val index = 1
        var newContext = narrationStateMachine.transition(VerseStateTransition.RECORD_AGAIN, index)

        Assert.assertEquals(VerseItemState.RECORD_AGAIN_ACTIVE, newContext[index])

        newContext = narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORD_AGAIN, index)

        Assert.assertEquals(VerseItemState.RECORD_AGAIN_PAUSED, newContext[index])

        for (i in newContext.indices) {
            if (i < index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_PAUSED, newContext[i])
            } else if (i > index + 1) {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RESUME_RECORDING with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        var newContext = narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        newContext = narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Resume recording
        newContext = narrationStateMachine.transition(VerseStateTransition.RESUME_RECORDING, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Resume recording
        narrationStateMachine.transition(VerseStateTransition.RESUME_RECORDING, index)

        // Next
        val newContext = narrationStateMachine.transition(VerseStateTransition.NEXT, index + 1)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if (i == index + 1) {
                Assert.assertEquals(VerseItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, with none previously active and resuming recording of verse 3`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        // Records verse 1
        narrationStateMachine.transition(VerseStateTransition.RECORD, 0)
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, 0)
        narrationStateMachine.transition(VerseStateTransition.NEXT, 1)

        // Records verse 2
        narrationStateMachine.transition(VerseStateTransition.RECORD, 1)
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, 1)
        narrationStateMachine.transition(VerseStateTransition.NEXT, 2)

        // Starts recording for verse 3
        narrationStateMachine.transition(VerseStateTransition.RECORD, 2)
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, 2)
        val newContext = narrationStateMachine.transition(VerseStateTransition.RESUME_RECORDING, 2)

        for (i in newContext.indices) {
            if (i < 2) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if (i == 2) {
                Assert.assertEquals(VerseItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Next
        val newContext = narrationStateMachine.transition(VerseStateTransition.NEXT, index + 1)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN, newContext[i])
            } else if (i == index + 1) {
                Assert.assertEquals(VerseItemState.RECORD, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Next
        var newContext = narrationStateMachine.transition(VerseStateTransition.NEXT, index + 1)

        // Verify that newContext[index] is in teh RECORD_AGAIN state
        Assert.assertEquals(VerseItemState.RECORD_AGAIN, newContext[index])

        newContext = narrationStateMachine.transition(VerseStateTransition.RECORD_AGAIN, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, then PAUSE_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Next
        var newContext = narrationStateMachine.transition(VerseStateTransition.NEXT, index + 1)

        newContext = narrationStateMachine.transition(VerseStateTransition.RECORD_AGAIN, index)

        // Verify that newContext[index] is in teh RECORD_AGAIN_ACTIVE state
        Assert.assertEquals(VerseItemState.RECORD_AGAIN_ACTIVE, newContext[index])

        // Pause recording
        newContext = narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORD_AGAIN, index)

        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_PAUSED, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, PAUSE_RECORD_AGAIN, RESUME_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(10) { false }

        narrationStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        narrationStateMachine.transition(VerseStateTransition.RECORD, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, index)

        // Next
        narrationStateMachine.transition(VerseStateTransition.NEXT, index + 1)

        narrationStateMachine.transition(VerseStateTransition.RECORD_AGAIN, index)

        // Pause recording
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORD_AGAIN, index)

        // Resume recording
        val newContext = narrationStateMachine.transition(VerseStateTransition.RESUME_RECORD_AGAIN, index)


        for (i in newContext.indices) {
            if (i == index) {
                Assert.assertEquals(VerseItemState.RECORD_AGAIN_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(VerseItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT, repeated until end of chapter, then SAVE with none previously active`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val narrationStateMachine = NarrationStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) { false }

        narrationStateMachine.initialize(activeVerses)

        // Puts all verses up to the last verse in a RECORD_AGAIN state
        var newContext: List<VerseItemState>? = null
        for (i in 0 until numMarkers - 1) {
            // Start recording
            narrationStateMachine.transition(VerseStateTransition.RECORD, i)

            // Pause recording
            narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, i)

            // Next
            newContext = narrationStateMachine.transition(VerseStateTransition.NEXT, i + 1)
        }

        // Records the last verse
        narrationStateMachine.transition(VerseStateTransition.RECORD, numMarkers - 1)

        // Pause
        narrationStateMachine.transition(VerseStateTransition.PAUSE_RECORDING, numMarkers - 1)

        // Saves
        newContext = narrationStateMachine.transition(VerseStateTransition.SAVE, numMarkers - 1)

        for (i in newContext?.indices!!) {
            Assert.assertEquals(VerseItemState.RECORD_AGAIN, newContext[i])
        }
    }

}