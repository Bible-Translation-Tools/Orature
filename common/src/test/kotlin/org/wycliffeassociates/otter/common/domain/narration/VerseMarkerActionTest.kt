package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class VerseMarkerActionTest {
    val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeTotalVerses()
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {}
    }


    // Initializes each verse with placed equal to true and with one sector
    // that is an int range of 44100*i until 44100*(i+1)
    // so each verseNode will have one second of recording
    fun initializeTotalVerses() {
        val numVerses = 31
        for(i in 0 until numVerses){
            val verseMarker = VerseMarker((i+1), (i+1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(0,0, true, verseMarker, sectors)
            sectors.add(44100*i until (44100*(i+1)))
            totalVerses.add(verseNode)
        }
    }

    // TODO: figure out if this is even a possibly case
    @Test
    fun `execute with null verseNodes`() {
        val emptyVerses: MutableList<VerseNode> = mutableListOf()
        val verseMarkerAction = VerseMarkerAction(0, 500)

        try {
            verseMarkerAction.execute(emptyVerses, workingAudioFile)
            Assert.fail("Expecting IllegalStateException")
        } catch (ise: IllegalStateException) {
            // Success: expecting illegalStateException
        }
    }


    @Test
    fun `execute with markerMovedBetweenVerses true and positive delta`() {
        val verseIndex = 1
        val delta = 500
        val verseMarkerAction = VerseMarkerAction(verseIndex, delta)

        // Verify that the verse being moved has correct initial values
        Assert.assertEquals(1, totalVerses[verseIndex].sectors.size)
        Assert.assertEquals(44100 * (verseIndex) until 44100 * (verseIndex + 1), totalVerses[verseIndex].sectors.first())

        // Verify that the verse before the verse being moved has correct initial values
        Assert.assertEquals(1, totalVerses[verseIndex - 1].sectors.size)
        Assert.assertEquals(44100 * (verseIndex - 1) until 44100 * (verseIndex), totalVerses[verseIndex - 1].sectors.first())

        verseMarkerAction.execute(totalVerses, workingAudioFile)

        // NOTE: it seems that we are takingFromEnd when we should be using takeFromStart.
        // Additionally, it seems that the nodes are copied to the wrong place
        // The names for verse and preceding verse seem to be swapped

        // Verify that the verse being moved has been updated correctly
        Assert.assertEquals((44100 * (verseIndex) + delta) until (44100 * (verseIndex + 1)), totalVerses[verseIndex].sectors.last())

        // Verify that the verse before the verse being moved is updated correctly
        Assert.assertEquals(44100  .. 44600, totalVerses[verseIndex - 1].sectors.last())

    }


    @Test
    fun `execute with markerMovedBetweenVerses true and negative delta`() {
        // TODO
        val verseIndex = 1
        val delta = -500
        val verseMarkerAction = VerseMarkerAction(verseIndex, delta)

        // Verify that the verse being moved has correct initial values
        Assert.assertEquals(1, totalVerses[verseIndex].sectors.size)
        Assert.assertEquals(
            44100 * (verseIndex) until 44100 * (verseIndex + 1),
            totalVerses[verseIndex].sectors.first()
        )

        // Verify that the verse before the verse being moved has correct initial values
        Assert.assertEquals(1, totalVerses[verseIndex - 1].sectors.size)
        Assert.assertEquals(
            44100 * (verseIndex - 1) until 44100 * (verseIndex),
            totalVerses[verseIndex - 1].sectors.first()
        )

        verseMarkerAction.execute(totalVerses, workingAudioFile)


        // NOTE: this is failing because the order in which the sectors are added is not correct.
        // Since we are moving the verseNode back, we should be taking frames from the end of the previous
        // verseNode and appending them to the start of the sectors list in the current verseNode.
        // Currently, they are appended to the end of the current verseNode.
        // Verify that the verse being moved has been updated correctly
        Assert.assertEquals(
            (44100*verseIndex + delta) until (44100*(verseIndex) - 1),
            totalVerses[verseIndex].sectors.first()
        )
        Assert.assertEquals(
            (44100*verseIndex) until (44100*(verseIndex+1)),
            totalVerses[verseIndex].sectors.last()
        )

        // Verify that the verse before the verse being moved is updated correctly
        Assert.assertEquals(
            (44100*(verseIndex - 1)) until 44100*(verseIndex) + delta,
            totalVerses[verseIndex - 1].sectors.last()
        )

    }

    @Test
    fun `execute with firstMarkerMoved true`() {
        // TODO
        // NOTE: not sure how to test this or when this happens

    }

}