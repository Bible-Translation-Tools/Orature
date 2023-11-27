package org.wycliffeassociates.otter.common.domain.narration.teleprompter

import io.mockk.mockk
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.AudioMarker

class TeleprompterStateMachineTest {

    fun mockAudioMarker() : AudioMarker {
        return mockk<AudioMarker>{}
    }

    fun makeAudioMarkerLists(size: Int) : List<AudioMarker> {
        return List(size) { mockAudioMarker() }
    }

    @Test
    fun `transition from RECORD to RECORD with multiple items in context and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0
        val newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Verifies that the state at index is RECORD_ACTIVE, and that the rest are RECORD_DISABLED
        for(i in audioMarkers.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD to RECORDING_PAUSED and none active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)


        val index = 0
        var newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Verify that newContext[index] is in the RECORD_ACTIVE state.
        Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[index])

        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORDING_PAUSED, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD_AGAIN then RECORD_AGAIN_PAUSED with first two active and recording second verse`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = MutableList(10) {false}
        activeVerses[0] = true
        activeVerses[1] = true

        teleprompterStateMachine.initialize(activeVerses)

        val index = 1
        var newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD_AGAIN, index)

        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[index])

        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORD_AGAIN, index)

        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[index])

        for(i in newContext.indices) {
            if(i < index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[i])
            } else if (i > index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RESUME_RECORDING with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        var newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Resume recording
        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RESUME_RECORDING, index)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Resume recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RESUME_RECORDING, index)

        // Next
        val newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, index + 1)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if(i == index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RESUME_RECORDING, with none previously active and resuming recording of verse 3`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        // Records verse 1
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, 0)
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, 0)
        teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, 1)

        // Records verse 2
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, 1)
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, 1)
        teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, 2)

        // Starts recording for verse 3
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, 2)
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, 2)
        val newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RESUME_RECORDING, 2)

        for(i in newContext.indices) {
            if(i < 2) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_DISABLED, newContext[i])
            } else if(i == 2) {
                Assert.assertEquals(TeleprompterItemState.RECORD_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }


    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Next
        val newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, index + 1)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i])
            } else if(i == index + 1) {
                Assert.assertEquals(TeleprompterItemState.RECORD, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Next
        var newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, index + 1)

        // Verify that newContext[index] is in teh RECORD_AGAIN state
        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[index])

        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD_AGAIN, index)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, then PAUSE_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Next
        var newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, index + 1)

        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD_AGAIN, index)

        // Verify that newContext[index] is in teh RECORD_AGAIN_ACTIVE state
        Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[index])

        // Pause recording
        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORD_AGAIN, index)

        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_PAUSED, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }

    @Test
    fun `transition from RECORD, RECORDING_PAUSED, RECORD_AGAIN, PAUSE_RECORD_AGAIN, RESUME_RECORD_AGAIN with none previously active`() {
        val audioMarkers = makeAudioMarkerLists(10)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(10) {false}

        teleprompterStateMachine.initialize(activeVerses)

        val index = 0

        // Start recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, index)

        // Next
        teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, index + 1)

        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD_AGAIN, index)

        // Pause recording
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORD_AGAIN, index)

        // Resume recording
        val newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.RESUME_RECORD_AGAIN, index)


        for(i in newContext.indices) {
            if(i == index) {
                Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN_ACTIVE, newContext[i])
            } else {
                Assert.assertEquals(TeleprompterItemState.RECORD_DISABLED, newContext[i])
            }
        }
    }
    
    @Test
    fun `transition from RECORD, RECORDING_PAUSED, then NEXT, repeated until end of chapter, then SAVE with none previously active`() {
        val numMarkers = 10
        val audioMarkers = makeAudioMarkerLists(numMarkers)
        val teleprompterStateMachine = TeleprompterStateMachine(audioMarkers)
        val activeVerses = List(numMarkers) {false}

        teleprompterStateMachine.initialize(activeVerses)

        // Puts all verses up to the last verse in a RECORD_AGAIN state
        var newContext: List<TeleprompterItemState>? = null
        for(i in 0 until numMarkers - 1) {
            // Start recording
            teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, i)

            // Pause recording
            teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, i)

            // Next
            newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.NEXT, i + 1)
        }

        // Records the last verse
        teleprompterStateMachine.transition(TeleprompterStateTransition.RECORD, numMarkers - 1)

        // Pause
        teleprompterStateMachine.transition(TeleprompterStateTransition.PAUSE_RECORDING, numMarkers - 1)

        // Saves
        newContext = teleprompterStateMachine.transition(TeleprompterStateTransition.SAVE, numMarkers - 1)

        for(i in newContext?.indices!!) {
            Assert.assertEquals(TeleprompterItemState.RECORD_AGAIN, newContext[i])
        }
    }



}