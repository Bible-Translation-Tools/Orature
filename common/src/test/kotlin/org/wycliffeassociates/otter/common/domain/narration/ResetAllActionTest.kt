package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class ResetAllActionTest {
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

    @Test
    fun `execute with placed verses and sectors`() {

        // verify that the data has been set up properly
        Assert.assertTrue(checkIfAnySectorsExists(totalVerses))
        Assert.assertTrue(checkIfAllVerseNodesArePlaced(totalVerses))

        val resetAllAction = ResetAllAction()

        resetAllAction.execute(totalVerses, workingAudioFile)

        // verify that totalVerses has been cleared
        Assert.assertFalse(checkIfAnySectorsExists(totalVerses))
        Assert.assertFalse(checkIfAllVerseNodesArePlaced(totalVerses))
    }

    fun checkIfAllVerseNodesArePlaced(verseNodes : MutableList<VerseNode>) : Boolean {
        for(verseNode in verseNodes) {
            if (!verseNode.placed) return false
        }
        return true
    }

    fun checkIfAnySectorsExists(verseNodes : MutableList<VerseNode>) : Boolean {
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

        val resetAllAction = ResetAllAction()

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

        val resetAllAction = ResetAllAction()

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