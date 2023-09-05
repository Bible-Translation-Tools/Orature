package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class NewVerseActionTest {

    val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeTotalVerses()
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {
            every { totalFrames } returns 411000
        }
    }

    fun initializeTotalVerses() {
        val numVerses = 31
        for(i in 0 until numVerses){
            val verseMarker = VerseMarker((i+1), (i+1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(0,0, false, verseMarker, sectors)
            totalVerses.add(verseNode)
        }
    }


    @Test
    fun `execute with valid working audio, 31 total verses, and no placed verses`() {
        val verseIndex = 0
        val newVerseAction = NewVerseAction(verseIndex)

        Assert.assertEquals(0, totalVerses[verseIndex].sectors.size)
        Assert.assertFalse(totalVerses[verseIndex].placed)

        Assert.assertNull(newVerseAction.node)

        newVerseAction.execute(totalVerses, workingAudioFile)

        // verify that NewVerseAction.node is valid
        Assert.assertEquals(411000..411000, newVerseAction.node?.sectors?.last())
        Assert.assertEquals(true, newVerseAction?.node?.placed)

        // verify that totalVerses[verseIndex] is valid
        Assert.assertEquals(411000..411000, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }


    @Test
    fun `execute with valid working audio, 31 total verses, and placed verses`() {
        val verseIndex = 0
        val newVerseAction = NewVerseAction(verseIndex)

        totalVerses[verseIndex].placed = true

        Assert.assertEquals(0, totalVerses[verseIndex].sectors.size)
        Assert.assertTrue(totalVerses[verseIndex].placed)

        Assert.assertNull(newVerseAction.node)

        newVerseAction.execute(totalVerses, workingAudioFile)

        // verify that NewVerseAction.node is valid
        Assert.assertEquals(411000..411000, newVerseAction.node?.sectors?.last())
        Assert.assertEquals(true, newVerseAction?.node?.placed)

        // verify that totalVerses[verseIndex] is valid
        Assert.assertEquals(411000..411000, totalVerses[verseIndex].sectors.last())
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `undo with invalid verseIndex`() {
        val verseIndex = -1
        val newVerseAction = NewVerseAction(verseIndex)

        try {
            newVerseAction.undo(totalVerses)
            Assert.fail("expecting IndexOutOfBoundsException")
        } catch (illegalIndex: IndexOutOfBoundsException){
            // Success: expecting IndexOutOfBoundsException
        }
    }

    @Test
    fun `undo with valid verseIndex`() {
        val verseIndex = 0

        // place verse
        val newVerseAction = NewVerseAction(verseIndex)

        newVerseAction.execute(totalVerses, workingAudioFile)

        // verify that it was placed
        Assert.assertTrue(totalVerses[verseIndex].placed)

        // undo
        newVerseAction.undo(totalVerses)

        // verify that it was undone
        Assert.assertFalse(totalVerses[verseIndex].placed)
    }


    @Test
    fun `redo with valid verseIndex`() {
        val verseIndex = 0

        // place verse
        val newVerseAction = NewVerseAction(verseIndex)

        newVerseAction.execute(totalVerses, workingAudioFile)

        // verify that it was placed
        Assert.assertTrue(totalVerses[verseIndex].placed)

        // undo
        newVerseAction.undo(totalVerses)

        // verify that it was undone
        Assert.assertFalse(totalVerses[verseIndex].placed)

        // redo
        newVerseAction.redo(totalVerses)

        // verify that it was redone
        Assert.assertTrue(totalVerses[verseIndex].placed)
    }

    @Test
    fun `redo with null node and valid verseIndex`() {
        val verseIndex = 0

        // place verse
        val newVerseAction = NewVerseAction(verseIndex)

        // redo
        newVerseAction.redo(totalVerses)

        // verify that nothing changed
        Assert.assertFalse(totalVerses[verseIndex].placed)
    }


    @Test
    fun `finalize with null node and valid verseIndex`() {
        val verseIndex = 0

        // place verse
        val newVerseAction = NewVerseAction(verseIndex)

        // redo
        newVerseAction.finalize(88200, totalVerses)

        // verify that nothing changed
        Assert.assertFalse(totalVerses[verseIndex].placed)

        Assert.assertEquals(0, totalVerses[verseIndex].sectors.size)
    }


    @Test
    fun `finalize with verseNode that has been started, but not finalized`() {
        val verseIndex = 0

        // place/start verse
        val newVerseAction = NewVerseAction(verseIndex)

        newVerseAction.execute(totalVerses, workingAudioFile)

        // verify that it was placed
        Assert.assertTrue(totalVerses[verseIndex].placed)

        val lastSectorStart = totalVerses[verseIndex].sectors.last().first
        val newLastSectorEnd = 882000

        // finalizes verseNode
        newVerseAction.finalize(newLastSectorEnd, totalVerses)

        // Verifies that finalization is correct
        // NOTE: Failure seems to be an issues with swapping inclusive and exclusive
        Assert.assertEquals(lastSectorStart..newLastSectorEnd, totalVerses[verseIndex].sectors.last())
    }
}