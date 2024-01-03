package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker

class ChapterEditedActionTest {
    private val totalVerses: MutableList<VerseNode> = mutableListOf()
    lateinit var workingAudioFile: AudioFile
    val numTestVerses = 31

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

    // Initializes each verse with placed equal to true and with one sector
    // that is an int range of 1000*i until 1000*(i+1)
    // so each verseNode will have one second of recording
    private fun makeNewListOfVerseNodes(numVerses: Int): MutableList<VerseNode> {
        val newVerseList: MutableList<VerseNode> = mutableListOf()
        for (i in 0 until numVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(true, verseMarker, sectors)
            sectors.add(1000 * i until (1000 * (i + 1)))
            newVerseList.add(verseNode)
        }
        return newVerseList
    }

    @Test
    fun `execute with empty newList`() {
        val emptyVerseList: MutableList<VerseNode> = mutableListOf()
        val chapterEditedAction = ChapterEditedAction(emptyVerseList)

        Assert.assertTrue(totalVerses.size > 0)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(0, totalVerses.size)
    }

    @Test
    fun `execute with non-empty newList`() {
        val newVerseList: MutableList<VerseNode> = makeNewListOfVerseNodes(10)
        val chapterEditedAction = ChapterEditedAction(newVerseList)

        Assert.assertEquals(31, totalVerses.size)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(10, totalVerses.size)
    }

    @Test
    fun `undo after execute with empty newList`() {
        val emptyVerseList: MutableList<VerseNode> = mutableListOf()
        val chapterEditedAction = ChapterEditedAction(emptyVerseList)

        Assert.assertTrue(totalVerses.size > 0)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(0, totalVerses.size)

        chapterEditedAction.undo(totalVerses)
        Assert.assertTrue(totalVerses.size > 0)
    }

    @Test
    fun `undo after execute with non-empty newList`() {
        val newVerseList: MutableList<VerseNode> = makeNewListOfVerseNodes(10)
        val chapterEditedAction = ChapterEditedAction(newVerseList)

        Assert.assertEquals(numTestVerses, totalVerses.size)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(10, totalVerses.size)

        chapterEditedAction.undo(totalVerses)
        Assert.assertEquals(numTestVerses, totalVerses.size)
    }

    @Test
    fun `redo after undo after execute with empty newList`() {
        val emptyVerseList: MutableList<VerseNode> = mutableListOf()
        val chapterEditedAction = ChapterEditedAction(emptyVerseList)

        Assert.assertTrue(totalVerses.size > 0)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(0, totalVerses.size)

        chapterEditedAction.undo(totalVerses)
        Assert.assertTrue(totalVerses.size > 0)

        chapterEditedAction.redo(totalVerses)
        Assert.assertEquals(0, totalVerses.size)
    }

    @Test
    fun `redo after undo after execute with non-empty newList`() {
        val newVerseList: MutableList<VerseNode> = makeNewListOfVerseNodes(10)
        val chapterEditedAction = ChapterEditedAction(newVerseList)

        Assert.assertEquals(numTestVerses, totalVerses.size)
        chapterEditedAction.execute(totalVerses, workingAudioFile)
        Assert.assertEquals(10, totalVerses.size)

        chapterEditedAction.undo(totalVerses)
        Assert.assertEquals(numTestVerses, totalVerses.size)

        chapterEditedAction.redo(totalVerses)
        Assert.assertEquals(10, totalVerses.size)
    }
}
