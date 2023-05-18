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
        val file = makeNewFile(100)
        addNextVerse(file)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.last().start, 0)
        assertEquals(narrationList.last().end, file.length().toInt())
    }

    @Test
    fun testOnNextVerseOnNonEmptyList() {
        val file = makeNewFile(100)
        addNextVerse(file)

        val newFile = makeNewFile(200)
        addNextVerse(newFile)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, file.length().toInt())
        assertEquals(narrationList.last().start, file.length().toInt())
        assertEquals(narrationList.last().end, newFile.length().toInt())
    }

    @Test
    fun testUndoOnNextSingleVerse() {
        val file = makeNewFile(100)
        addNextVerse(file)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo()

        assertEquals(narrationList.size, 0)
    }

    @Test
    fun testUndoOnNextMultipleVerses() {
        val firstFile = makeNewFile(100)
        addNextVerse(firstFile)

        val secondFile = makeNewFile(200)
        addNextVerse(secondFile)

        assertEquals(narrationList.size, 2)

        narrationHistory.undo()

        assertEquals(narrationList.size, 1)
    }

    @Test
    fun testRedoOnNextSingleVerse() {
        val file = makeNewFile(100)
        addNextVerse(file)

        assertEquals(narrationList.size, 1)

        narrationHistory.undo()

        assertEquals(narrationList.size, 0)

        narrationHistory.redo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, file.length().toInt())
    }

    @Test
    fun testRerecordVerse() {
        val file = makeNewFile(100)
        addNextVerse(file)

        assertEquals(narrationList.size, 1)

        val reRecordedFile = makeNewFile(200)
        val action = RerecordAction(narrationList, reRecordedFile, 0)

        narrationHistory.execute(action)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, file.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
    }

    @Test
    fun testRerecordVerseInTheMiddle() {
        val firstFile = makeNewFile(100)
        addNextVerse(firstFile)

        val secondFile = makeNewFile(200)
        addNextVerse(secondFile)

        val thirdFile = makeNewFile(300)
        addNextVerse(thirdFile)

        assertEquals(narrationList.size, 3)

        val reRecordedFile = makeNewFile(400)
        // Rerecording 2nd file, on index 1
        val action = RerecordAction(narrationList, reRecordedFile, 1)

        narrationHistory.execute(action)

        assertEquals(narrationList.size, 3)
        assertEquals(narrationList[1].start, thirdFile.length().toInt())
        assertEquals(narrationList[1].end, reRecordedFile.length().toInt())
    }

    @Test
    fun testUndoRedoRerecordVerse() {
        val file = makeNewFile(100)
        addNextVerse(file)

        val reRecordedFile = makeNewFile(200)
        val action = RerecordAction(narrationList, reRecordedFile, 0)
        narrationHistory.execute(action)

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, file.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())

        narrationHistory.undo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, file.length().toInt())

        narrationHistory.redo()

        assertEquals(narrationList.size, 1)
        assertEquals(narrationList.first().start, file.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
    }

    @Test
    fun testChangeVerseMarker() {
        val file = makeNewFile(100)
        addNextVerse(file)

        val anotherFile = makeNewFile(200)
        addNextVerse(anotherFile)

        val newMarker = 1_200

        val action = MarkerAction(narrationList, 0, 1, newMarker)
        narrationHistory.execute(action)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, anotherFile.length().toInt())
    }

    @Test
    fun testUndoRedoChangeVerseMarker() {
        val file = makeNewFile(100)
        addNextVerse(file)

        val anotherFile = makeNewFile(200)
        addNextVerse(anotherFile)

        val newMarker = 1_200

        val action = MarkerAction(narrationList, 0, 1, newMarker)
        narrationHistory.execute(action)

        narrationHistory.undo()

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, file.length().toInt())
        assertEquals(narrationList.last().start, file.length().toInt())
        assertEquals(narrationList.last().end, anotherFile.length().toInt())

        narrationHistory.redo()

        assertEquals(narrationList.first().start, 0)
        assertEquals(narrationList.first().end, newMarker)
        assertEquals(narrationList.last().start, newMarker)
        assertEquals(narrationList.last().end, anotherFile.length().toInt())
    }

    @Test
    fun testNewVerseAfterRerecordPreviousVerse() {
        val firstFile = makeNewFile(100)
        addNextVerse(firstFile)

        val reRecordedFile = makeNewFile(200)
        val reRecordAction = RerecordAction(narrationList, reRecordedFile, 0)
        narrationHistory.execute(reRecordAction)

        val secondFile = makeNewFile(300)
        addNextVerse(secondFile)

        assertEquals(narrationList.size, 2)
        assertEquals(narrationList.first().start, firstFile.length().toInt())
        assertEquals(narrationList.first().end, reRecordedFile.length().toInt())
        assertEquals(narrationList.last().start, reRecordedFile.length().toInt())
        assertEquals(narrationList.last().end, secondFile.length().toInt())
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
}