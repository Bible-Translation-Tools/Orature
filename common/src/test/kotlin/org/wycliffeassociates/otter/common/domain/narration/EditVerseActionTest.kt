package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class EditVerseActionTest {
    private val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile
    val numTestVerses = 31

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeVerseNodeList(totalVerses)
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {
            every { totalFrames } returns 44100 * numTestVerses
        }
    }

    // Initializes each verse with placed equal to true and with one sector that holds one second worth of frames.
    // where the start of each added sector is offset by "paddingLength" number of frames
    private fun initializeVerseNodeList(
        verseNodeList: MutableList<VerseNode>,
        paddingLength: Int = 0,
    ) {
        var start = -1
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(true, verseMarker, sectors)
            sectors.add(start + 1..start + 44100)
            start += 44100 + paddingLength
            verseNodeList.add(verseNode)
        }
    }

    @Test
    fun `execute with empty verseNode list`() {
        val emptyVerses: MutableList<VerseNode> = mutableListOf()
        val editVerseAction = EditVerseAction(0, 1000, 2000)

        try {
            editVerseAction.execute(emptyVerses, workingAudioFile)
            Assert.fail("Expecting IndexOutOfBounds")
        } catch (indexOutOfBounds: IndexOutOfBoundsException) {
            // Success: expecting IndexOutOfBoundsException
        }
    }

    @Test
    fun `execute with null previous and node and stating edit at end of working audio`() {
        val verseEditedIndex = 0
        val verseBeingEdited = totalVerses[verseEditedIndex]

        // Starts new verse edit at the end of the audio file + 1
        val editVerseStart = workingAudioFile.totalFrames
        // Sets the end of the new verse edit to some frames after the start
        val editVerseEnd = editVerseStart + 44100

        val editVerseAction = EditVerseAction(verseEditedIndex, editVerseStart, editVerseEnd)

        // Verify that the verse being moved has the expected values
        Assert.assertEquals(0..44099, verseBeingEdited.sectors.last())

        // Verify that the editVerseAction has not been used before and is initialized correctly
        Assert.assertNull(editVerseAction.previous)
        Assert.assertNull(editVerseAction.node)

        editVerseAction.execute(totalVerses, workingAudioFile)

        // Verify that editVerseAction's node and previous are correct values
        Assert.assertEquals(1, editVerseAction.previous?.sectors?.size)
        Assert.assertEquals(0..44099, editVerseAction.previous?.sectors?.last())
        Assert.assertEquals(1, editVerseAction.node?.sectors?.size)
        Assert.assertEquals(editVerseStart..editVerseEnd, editVerseAction.node?.sectors?.last())

        // Verify that the edited verse has the correct values after being edited
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(editVerseStart..editVerseEnd, totalVerses[verseEditedIndex].sectors.last())
        Assert.assertEquals(true, totalVerses[verseEditedIndex].placed)
    }

    @Test
    fun `undo with empty verseNode list`() {
        val emptyVerses: MutableList<VerseNode> = mutableListOf()
        val editVerseAction = EditVerseAction(0, 1000, 2000)

        // Verify that the editVerseAction has correct initial values for node and previous
        Assert.assertNull(editVerseAction.node)
        Assert.assertNull(editVerseAction.previous)

        editVerseAction.undo(emptyVerses)

        // Verify that the editVerseAction's node and previous have not changed
        Assert.assertNull(editVerseAction.node)
        Assert.assertNull(editVerseAction.previous)

        // Verify that emptyVerses has not changed
        Assert.assertEquals(0, emptyVerses.size)
    }

    @Test
    fun `undo after execute with null previous and node and stating edit at end of working audio`() {
        val verseEditedIndex = 0
        val verseBeingEdited = totalVerses[verseEditedIndex]

        // Starts new verse edit at the end of the audio file + 1
        val editVerseStart = workingAudioFile.totalFrames
        // Sets the end of the new verse edit to some frames after the start
        val editVerseEnd = editVerseStart + 44100

        // Verify that the verse being moved has the expected initial values
        Assert.assertEquals(1, verseBeingEdited.sectors.size)
        Assert.assertEquals(0..44099, verseBeingEdited.sectors.last())

        val editVerseAction = EditVerseAction(verseEditedIndex, editVerseStart, editVerseEnd)
        editVerseAction.execute(totalVerses, workingAudioFile)

        // Verify that the edited verse has the correct values after being edited
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(editVerseStart..editVerseEnd, totalVerses[verseEditedIndex].sectors.last())
        Assert.assertEquals(true, totalVerses[verseEditedIndex].placed)

        // undo the edit
        editVerseAction.undo(totalVerses)

        // Verify that the verse has been restored to its initial state
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(0..44099, totalVerses[verseEditedIndex].sectors.last())
    }

    @Test
    fun `redo with empty verseNode list`() {
        val emptyVerses: MutableList<VerseNode> = mutableListOf()
        val editVerseAction = EditVerseAction(0, 1000, 2000)

        // Verify that the editVerseAction has correct initial values for node and previous
        Assert.assertNull(editVerseAction.node)
        Assert.assertNull(editVerseAction.previous)

        editVerseAction.redo(emptyVerses)

        // Verify that the editVerseAction's node and previous have not changed
        Assert.assertNull(editVerseAction.node)
        Assert.assertNull(editVerseAction.previous)

        // Verify that emptyVerses has not changed
        Assert.assertEquals(0, emptyVerses.size)
    }

    @Test
    fun `redo after undo after execute with null previous and node and stating edit at end of working audio`() {
        val verseEditedIndex = 0
        val verseBeingEdited = totalVerses[verseEditedIndex]

        // Starts new verse edit at the end of the audio file + 1
        val editVerseStart = workingAudioFile.totalFrames
        // Sets the end of the new verse edit to some frames after the start
        val editVerseEnd = editVerseStart + 44100

        // Verify that the verse being moved has the expected initial values
        Assert.assertEquals(1, verseBeingEdited.sectors.size)
        Assert.assertEquals(0..44099, verseBeingEdited.sectors.last())

        val editVerseAction = EditVerseAction(verseEditedIndex, editVerseStart, editVerseEnd)
        editVerseAction.execute(totalVerses, workingAudioFile)

        // Verify that the edited verse has the correct values after being edited
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(editVerseStart..editVerseEnd, totalVerses[verseEditedIndex].sectors.last())
        Assert.assertEquals(true, totalVerses[verseEditedIndex].placed)

        // undo the edit
        editVerseAction.undo(totalVerses)

        // Verify that the verse has been restored to its initial state
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(0..44099, totalVerses[verseEditedIndex].sectors.last())

        // redo the edit
        editVerseAction.redo(totalVerses)

        // Verify that the previous edit has been restored
        Assert.assertEquals(1, totalVerses[verseEditedIndex].sectors.size)
        Assert.assertEquals(editVerseStart..editVerseEnd, totalVerses[verseEditedIndex].sectors.last())
        Assert.assertEquals(true, totalVerses[verseEditedIndex].placed)
    }
}
