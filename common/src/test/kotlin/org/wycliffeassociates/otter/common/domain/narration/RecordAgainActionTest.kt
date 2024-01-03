package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class RecordAgainActionTest {
    private val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile
    val numTestVerses = 31

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeTotalVerses()
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {
            every { totalFrames } returns numTestVerses * 44100
        }
    }

    fun initializeTotalVerses() {
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(false, verseMarker, sectors)
            totalVerses.add(verseNode)
        }
    }

    fun placeFirstVerseWithOneSecondAudio() {
        totalVerses[0].placed = true
        totalVerses[0].sectors.add(0..44099)
    }

    @Test
    fun `execute with valid working audio, 31 total verses, and no placed verses`() {
        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)

        Assert.assertEquals(0, totalVerses[verseIndex].sectors.size)
        Assert.assertFalse(totalVerses[verseIndex].placed)

        Assert.assertNull(recordAgainAction.node)

        recordAgainAction.execute(totalVerses, workingAudioFile)

        // verify that RecordAgainAction.node is valid
        val expectedEnd = workingAudioFile.totalFrames + 1
        Assert.assertEquals(expectedEnd..expectedEnd, recordAgainAction.node?.sectors?.last())
        Assert.assertEquals(true, recordAgainAction.node?.placed)

        // verify that RecordAgainAction.previous is valid
        Assert.assertEquals(0, recordAgainAction.previous?.sectors?.size)
        Assert.assertEquals(false, recordAgainAction.previous?.placed)

        // verify that totalVerses[verseIndex] is valid
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `execute with valid working audio, 31 total verses, and one placed verse`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)

        val previousNode = totalVerses[verseIndex].copy()

        Assert.assertFalse(totalVerses[verseIndex].sectors.size == 0)
        Assert.assertTrue(totalVerses[verseIndex].placed)

        Assert.assertNull(recordAgainAction.node)

        recordAgainAction.execute(totalVerses, workingAudioFile)

        // verify that RecordAgainAction.node is valid
        val expectedEnd = workingAudioFile.totalFrames + 1
        Assert.assertEquals(expectedEnd..expectedEnd, recordAgainAction.node?.sectors?.last())
        Assert.assertEquals(true, recordAgainAction.node?.placed)

        // verify that RecordAgainAction.previous is valid
        Assert.assertEquals(previousNode.sectors.last(), recordAgainAction.previous?.sectors?.last())
        Assert.assertEquals(true, recordAgainAction.previous?.placed)

        // verify that totalVerses[verseIndex] is valid
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `execute with invalid index`() {
        val verseIndex = -1
        val recordAgainAction = RecordAgainAction(verseIndex)

        try {
            recordAgainAction.execute(totalVerses, workingAudioFile)
            Assert.fail("expecting IndexOutOfBoundsException")
        } catch (illegalIndex: IndexOutOfBoundsException) {
            // Success: expecting IndexOutOfBoundsException
        }
    }

    @Test
    fun `undo with null previous`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)
        val previousNode = totalVerses[verseIndex].copy()

        recordAgainAction.undo(totalVerses)

        Assert.assertEquals(previousNode.sectors.last(), totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(previousNode.placed)
    }

    @Test
    fun `undo with non-null previous and valid verseIndex`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)
        val previousNode = totalVerses[verseIndex].copy()

        recordAgainAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses[verseIndex] is valid
        val expectedEnd = workingAudioFile.totalFrames + 1
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)

        recordAgainAction.undo(totalVerses)

        // verify that totalVerses[verseIndex] is undone
        Assert.assertEquals(previousNode.sectors.last(), totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `redo with non-null previous and valid verseIndex`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)
        val previousNode = totalVerses[verseIndex].copy()

        recordAgainAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses[verseIndex] is valid
        val expectedEnd = workingAudioFile.totalFrames + 1
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)

        recordAgainAction.undo(totalVerses)

        // verify that totalVerses[verseIndex] is undone
        Assert.assertEquals(previousNode.sectors.last(), totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)

        recordAgainAction.redo(totalVerses)

        // verify that totalVerses[verseIndex] is redone
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `finalize before execute`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)
        val previousNode = totalVerses[verseIndex].copy()

        recordAgainAction.finalize(88200, totalVerses)

        // Verify that totalVerses[verseIndex] was not changed
        Assert.assertEquals(previousNode.sectors.last(), totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(previousNode.placed)

        // Verify that recordAgainAction.node is unchanged
        Assert.assertNull(recordAgainAction.node)
    }

    @Test
    fun `finalize after execute`() {
        placeFirstVerseWithOneSecondAudio()

        val verseIndex = 0
        val recordAgainAction = RecordAgainAction(verseIndex)
        val previousNode = totalVerses[verseIndex].copy()

        recordAgainAction.execute(totalVerses, workingAudioFile)

        // Verify that totalVerse[verseIndex] has been updated
        val expectedEnd = workingAudioFile.totalFrames + 1
        Assert.assertEquals(expectedEnd..expectedEnd, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(previousNode.placed)

        recordAgainAction.finalize(882000, totalVerses)

        // Verify that totalVerse[verseIndex] has been finalized
        Assert.assertEquals(expectedEnd..882000, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(previousNode.placed)
    }
}
