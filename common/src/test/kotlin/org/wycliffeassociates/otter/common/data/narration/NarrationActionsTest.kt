package org.wycliffeassociates.otter.common.data.narration

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import java.io.File
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
    fun testOnNextVerseOnEmptyList() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.last().start, 0)
        assertEquals(narrationList.last().end, fileUpdated.length().toInt())
    }

    @Test
    fun testOnNextVerseOnNonEmptyList() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.length().toInt())
        assertEquals(narrationList.last().start, firstFileUpdated.length().toInt())
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())
    }

    @Test
    fun testUndoOnNextSingleVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo()

        assertEquals(narrationList.size, 0)
    }

    @Test
    fun testUndoOnNextMultipleVerses() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)

        narrationHistory.undo()

        assertEquals(narrationList.size, 1)
    }

    @Test
    fun testRedoOnNextSingleVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo()

        assertEquals(narrationList.size, 0)

        narrationHistory.redo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, fileUpdated.length().toInt())
    }

    @Test
    fun testRerecordVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), fileUpdated)

        assertEquals(narrationList.size, 1)

        val reRecordedFile = makeNewFile(200)
        val action = RerecordAction(narrationList, reRecordedFile, 0)

        narrationHistory.execute(action)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
    }

    @Test
    fun testRerecordVerseInTheMiddle() {
        val firstFile = makeNewFile(100)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList[1], secondFileUpdated)

        val thirdFile = makeNewFile(secondFileUpdated.length())
        addNextVerse(thirdFile)
        val thirdFileUpdated = makeNewFile(300)
        onPauseOrNext(narrationList.last(), thirdFileUpdated)

        assertEquals(narrationList.size, 3)

        val reRecordedFile = makeNewFile(400)
        // Rerecording 2nd file, on index 1
        val action = RerecordAction(narrationList, reRecordedFile, 1)

        narrationHistory.execute(action)

        assertEquals(narrationList.size, 3)
        assertEquals(narrationList[1].start, thirdFileUpdated.length().toInt())
        assertEquals(narrationList[1].end, reRecordedFile.length().toInt())
    }

    @Test
    fun testUndoRedoRerecordVerse() {
        val file = makeNewFile(0)
        addNextVerse(file)
        val fileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), fileUpdated)

        val reRecordedFile = makeNewFile(200)
        val action = RerecordAction(narrationList, reRecordedFile, 0)
        narrationHistory.execute(action)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())

        narrationHistory.undo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, fileUpdated.length().toInt())

        narrationHistory.redo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, fileUpdated.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
    }

    @Test
    fun testChangeVerseMarker() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        val newMarker = 120

        val action = MarkerAction(narrationList, 0, 1, newMarker)
        narrationHistory.execute(action)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())
    }

    @Test
    fun testUndoRedoChangeVerseMarker() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        val newMarker = 120

        val action = MarkerAction(narrationList, 0, 1, newMarker)
        narrationHistory.execute(action)

        narrationHistory.undo()

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.length().toInt())
        assertEquals(narrationList.last().start, secondFile.length().toInt())
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())

        narrationHistory.redo()

        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())
    }

    @Test
    fun testOnNextVerseAfterRerecordPreviousVerse() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val reRecordedFile = makeNewFile(200)
        val reRecordAction = RerecordAction(narrationList, reRecordedFile, 0)
        narrationHistory.execute(reRecordAction)

        val secondFile = makeNewFile(reRecordedFile.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(300)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, firstFileUpdated.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
        assertEquals(narrationList.last().start, secondFile.length().toInt())
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())
    }

    @Test
    fun testRedoIneffectiveAfterAnAction() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val reRecordedFile = makeNewFile(200)
        val reRecordAction = RerecordAction(narrationList, reRecordedFile, 0)
        narrationHistory.execute(reRecordAction)

        narrationHistory.undo()

        val secondFile = makeNewFile(reRecordedFile.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(300)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        narrationHistory.redo()

        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.length().toInt())

        assertEquals(narrationList.last().start, reRecordedFile.length().toInt())
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())
    }

    @Test
    fun testUndoRedoResetAllVerses() {
        val firstFile = makeNewFile(0)
        addNextVerse(firstFile)
        val firstFileUpdated = makeNewFile(100)
        onPauseOrNext(narrationList.first(), firstFileUpdated)

        val secondFile = makeNewFile(firstFileUpdated.length())
        addNextVerse(secondFile)
        val secondFileUpdated = makeNewFile(200)
        onPauseOrNext(narrationList.last(), secondFileUpdated)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, firstFileUpdated.length().toInt())
        assertEquals(narrationList.last().start, firstFileUpdated.length().toInt())
        assertEquals(narrationList.last().end, secondFileUpdated.length().toInt())

        val resetAction = ResetAllAction(narrationList)
        narrationHistory.execute(resetAction)

        assertEquals(narrationList.size, 0)

        narrationHistory.undo()

        assertEquals(narrationList.size, 2)
    }

    private fun addNextVerse(file: File) {
        val action = NextVerseAction(narrationList, file)
        narrationHistory.execute(action)
    }

    private fun makeNewFile(length: Long): File {
        return mock<File> {
            on { length() } doReturn length
        }
    }

    private fun onPauseOrNext(verse: VerseNode, file: File) {
        verse.end = file.length().toInt()
    }
}