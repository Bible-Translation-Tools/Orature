package org.wycliffeassociates.otter.common.domain.narration

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio

class ResetAllActionTest {
    private val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile
    val numTestVerses = 31
    private val mockAssociatedAudio = mock<AssociatedAudio> {
        on { getSelectedTake() } doReturn (null)
        on { selectTake(anyOrNull()) } doAnswer { }
    }

    @Before
    fun setup() {
        workingAudioFile = mockWorkingAudio()
        initializeVerseNodeList(totalVerses)
    }

    fun mockWorkingAudio(): AudioFile {
        return mockk<AudioFile> {}
    }

    // Initializes each verse with placed equal to true and with one sector that holds one second worth of frames.
    // where the start of each added sector is offset by "paddingLength" number of frames
    private fun initializeVerseNodeList(verseNodeList : MutableList<VerseNode>, paddingLength: Int = 0) {
        var start = -1
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(true, verseMarker, sectors)
            sectors.add(start + 1 .. start + 44100)
            start += 44100 + paddingLength
            verseNodeList.add(verseNode)
        }
    }

    @Test
    fun `execute with placed verses and sectors`() {

        // verify that the data has been set up properly
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))

        val resetAllAction = ResetAllAction(mockAssociatedAudio)

        resetAllAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses has been cleared
        Assert.assertFalse(checkIfAnySectorsExists(totalVerses))
        Assert.assertFalse(checkIfAllVerseNodesArePlaced(totalVerses))
    }

    private fun checkIfAllVerseNodesArePlaced(verseNodes : MutableList<VerseNode>) : Boolean {
        for(verseNode in verseNodes) {
            if (!verseNode.placed) return false
        }
        return true
    }

    private fun checkIfAnySectorsExists(verseNodes : MutableList<VerseNode>) : Boolean {
        for(verseNode in verseNodes) {
            if (verseNode.sectors.size != 0) return true
        }
        return false
    }

    @Test
    fun `undo with placed verses and sectors`() {
        // verify that the data has been set up properly
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))

        val resetAllAction = ResetAllAction(mockAssociatedAudio)

        resetAllAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses has been cleared
        Assert.assertFalse(checkIfAnySectorsExists(totalVerses))
        Assert.assertFalse(checkIfAllVerseNodesArePlaced(totalVerses))

        resetAllAction.undo(totalVerses)

        // verify that totalVerses has been restored
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))
    }

    @Test
    fun `redo with placed verses and sectors`() {
        // verify that the data has been set up properly
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))

        val resetAllAction = ResetAllAction(mockAssociatedAudio)

        resetAllAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses has been cleared
        Assert.assertFalse(checkIfAnySectorsExists(totalVerses))
        Assert.assertFalse(checkIfAllVerseNodesArePlaced(totalVerses))

        resetAllAction.undo(totalVerses)

        // verify that totalVerses has been restored
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))

        resetAllAction.redo(totalVerses)

        // verify that totalVerses has been undone
        Assert.assertFalse(checkIfAnySectorsExists(totalVerses))
        Assert.assertFalse(checkIfAllVerseNodesArePlaced(totalVerses))
    }
}