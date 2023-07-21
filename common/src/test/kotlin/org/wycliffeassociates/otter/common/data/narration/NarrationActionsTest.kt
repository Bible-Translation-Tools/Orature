package org.wycliffeassociates.otter.common.data.narration

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.domain.narration.*
import org.wycliffeassociates.otter.common.domain.narration.NarrationHistory
import java.util.ArrayList

class NarrationActionsTest {

    private val narrationList = ArrayList<VerseNode>()
    private val narrationHistory = NarrationHistory()

    @Before
    fun setup() {
        narrationList.clear()
        narrationHistory.clear()
    }

    @Test
    fun testOnNewVerseOnEmptyList() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.last().start, 0)
        assertEquals(narrationList.last().end, fileUpdated.totalFrames)
    }

    @Test
    fun testOnNextVerseOnNonEmptyList() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.totalFrames)
        assertEquals(narrationList.last().start, firstFileUpdated.totalFrames)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)
    }

    @Test
    fun testUndoOnNextSingleVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 0)
    }

    @Test
    fun testUndoOnNextMultipleVerses() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 1)
    }

    @Test
    fun testRedoOnNextSingleVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 0)

        narrationHistory.redo(narrationList)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, fileUpdated.totalFrames)
    }

    @Test
    fun testRerecordVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        val action = RecordAgainAction(0)
        narrationHistory.execute(action, narrationList, fileUpdated)
        val reRecordedFile = makeNewFile(200)
        onPauseOrNew(narrationList.first(), reRecordedFile)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.totalFrames)
        assertEquals(narrationList.first().end, reRecordedFile.totalFrames)
    }

    @Test
    fun testRerecordVerseInTheMiddle() {
        val firstFile = makeNewFile(100)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList[1], secondFileUpdated)

        val thirdFile = makeNewFile(secondFileUpdated.totalFrames)
        addNextVerse(thirdFile)
        val thirdFileUpdated = makeNewFile(300)
        onPauseOrNew(narrationList.last(), thirdFileUpdated)

        assertEquals(narrationList.size, 3)

        // Rerecording 2nd file, on index 1
        val action = RecordAgainAction(1)
        narrationHistory.execute(action, narrationList, thirdFileUpdated)
        val reRecordedFile = makeNewFile(400)
        onPauseOrNew(narrationList[1], reRecordedFile)

        assertEquals(narrationList.size, 3)
        assertEquals(narrationList[1].start, thirdFileUpdated.totalFrames)
        assertEquals(narrationList[1].end, reRecordedFile.totalFrames)
    }

    @Test
    fun testUndoRedoRerecordVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), fileUpdated)

        val action = RecordAgainAction(0)
        narrationHistory.execute(action, narrationList, fileUpdated)
        val reRecordedFile = makeNewFile(200)
        onPauseOrNew(narrationList.first(), reRecordedFile)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.totalFrames)
        assertEquals(narrationList.first().end, reRecordedFile.totalFrames)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, fileUpdated.totalFrames)

        narrationHistory.redo(narrationList)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.totalFrames)
        assertEquals(narrationList.first().end, reRecordedFile.totalFrames)
    }

    @Test
    fun testChangeVerseMarker() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        val newMarker = 120

        val action = VerseMarkerAction(1, newMarker)
        narrationHistory.execute(action, narrationList, secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)
    }

    @Test
    fun testUndoRedoChangeVerseMarker() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        val newMarker = 120

        val action = VerseMarkerAction(1, newMarker)
        narrationHistory.execute(action, narrationList, secondFileUpdated)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.totalFrames)
        assertEquals(narrationList.last().start, secondFile.totalFrames)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)

        narrationHistory.redo(narrationList)

        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)
    }

    @Test
    fun testOnNextVerseAfterRerecordPreviousVerse() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)


        val reRecordAction = RecordAgainAction(0)
        narrationHistory.execute(reRecordAction, narrationList, firstFileUpdated, )
        val reRecordedFile = makeNewFile(200)
        onPauseOrNew(narrationList.first(), reRecordedFile)

        val secondFile = makeNewFile(reRecordedFile.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(300)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, firstFileUpdated.totalFrames)
        assertEquals(narrationList.first().end, reRecordedFile.totalFrames)
        assertEquals(narrationList.last().start, secondFile.totalFrames)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)
    }

    @Test
    fun testRedoIneffectiveAfterAnAction() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val reRecordedFile = makeNewFile(200)
        val reRecordAction = RecordAgainAction(0)
        narrationHistory.execute(reRecordAction, narrationList, reRecordedFile)

        narrationHistory.undo(narrationList)

        val secondFile = makeNewFile(reRecordedFile.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(300)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        narrationHistory.redo(narrationList)

        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.totalFrames)

        assertEquals(narrationList.last().start, reRecordedFile.totalFrames)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)
    }

    @Test
    fun testUndoRedoResetAllVerses() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNew(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.totalFrames)
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNew(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.totalFrames)
        assertEquals(narrationList.last().start, firstFileUpdated.totalFrames)
        assertEquals(narrationList.last().end, secondFileUpdated.totalFrames)

        val resetAction = ResetAllAction()
        narrationHistory.execute(resetAction, narrationList, secondFileUpdated)

        assertEquals(narrationList.size, 0)

        narrationHistory.undo(narrationList)

        assertEquals(narrationList.size, 2)
    }

    private fun addNextVerse(audioFile: AudioFile) {
        val action = NewVerseAction()
        narrationHistory.execute(action, narrationList, audioFile)
    }

    private fun makeNewFile(length: Int): AudioFile {
        return mock<AudioFile> {
            on { totalFrames } doReturn length
        }
    }

    private fun onPauseOrNew(verse: VerseNode, audioFile: AudioFile) {
        verse.end = audioFile.totalFrames
    }
}