package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
const val testDirWithAudio =  "C:\\Users\\hilld\\testProjectChapterDirWithAudio"
const val testDirWithoutAudio = "C:\\Users\\hilld\\testProjectChapterDirWithoutAudio"

class ChapterRepresentationTest {

    lateinit var chapter: Chapter
    lateinit var workbookWithAudio: Workbook
    lateinit var workbookWithoutAudio: Workbook
    lateinit var chunk: Observable<Chunk>
    val numTestVerses = 31


    @Before
    fun setup() {
        chunk = createObservableChunkMock(mockChunk())
        workbookWithAudio = mockWorkbook(true)
        workbookWithoutAudio = mockWorkbook(false)
        chapter = mockChapter()
    }

    fun mockWorkbook(withAudio: Boolean) : Workbook {
        val audioDirectory = if (withAudio) testDirWithAudio else testDirWithoutAudio
        return mockk<Workbook>{
            every { projectFilesAccessor.getChapterAudioDir(any(), any())} returns File(audioDirectory)
        }
    }

    fun mockChapter() : Chapter {
        return mockk<Chapter> {
            every { getDraft() } returns chunk
        }
    }

    fun mockChunk() : Chunk {
        return mockk<Chunk>{
            every { start } returns 0
            every { end } returns 0
        }
    }

    private fun createObservableChunkMock(chunk: Chunk): Observable<Chunk> {
        return Observable.just(chunk)
    }

    // Initializes each verse with placed equal to true and with one sector that holds one second worth of frames.
    // where the start of each added sector is offset by "paddingLength" number of frames
    fun initializeVerseNodeList(verseNodeList : MutableList<VerseNode>, paddingLength: Int = 0) {
        var start = -1
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(0, 0, true, verseMarker, sectors)
            sectors.add(start + 1 .. start + 44100)
            start += 44100 + paddingLength
            verseNodeList.add(verseNode)
        }
    }

    // Simulates re-recording each verse for some number of frames (sectorLength), with some amount of "dead-space"
    // (spaceBetweenSectors) between each newly added sector
    fun addSectorsToEnd(verseNodeList : MutableList<VerseNode>, sectorLength : Int, spaceBetweenSectors: Int) {
        var lastSectorEnd = verseNodeList.last().sectors.last().last + 1
        for (i in 1 until verseNodeList.size) {
            val start = lastSectorEnd + spaceBetweenSectors
            val end = start + sectorLength
            lastSectorEnd = end
            verseNodeList[i].sectors.add(start until end)
        }
    }


    fun writeByteBufferToPCMFile(byteBuffer: ByteBuffer, filePath: String) {
        try {
            val byteArray = ByteArray(byteBuffer.remaining())
            byteBuffer.get(byteArray)

            val file = File(filePath)
            val fos = FileOutputStream(file)

            fos.write(byteArray)

            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        byteBuffer.rewind()
    }


    fun fillByteBuffferWithNoPadding(byteBuffer: ByteBuffer, secondsOfAudio: Int) {
        for (i in 1 .. secondsOfAudio) {
            for(j in 1 .. 44100) {
                byteBuffer.putShort(i.toShort())
            }
        }
        byteBuffer.rewind()
    }


    fun fillAudioBufferWithPadding(byteBuffer: ByteBuffer, secondsOfAudio: Int, paddingLength: Int) {
        for (i in 1 .. secondsOfAudio) {
            for(j in 1 .. 44100) {
                byteBuffer.putShort(i.toShort())
            }
            for(j in 1 .. paddingLength) {
                byteBuffer.putShort(0)
            }
        }
        byteBuffer.rewind()
    }

    @Test
    fun `totalFrames with initialized totalVerses list`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        Assert.assertEquals(44100*numTestVerses, chapterRepresentation.totalFrames)
    }


    // TODO: add test for loadFromSerializedVerses

    // TODO: add test for finalizeVerse

    @Test
    fun `absoluteToRelative with empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val relativePosition = chapterRepresentation.absoluteToRelative(1000)
        Assert.assertEquals(0, relativePosition)
    }

    @Test
    fun `absoluteToRelative with non-empty activeVerses and absoluteFrame not in activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val relativePosition = chapterRepresentation.absoluteToRelative(-5)
        Assert.assertEquals(0, relativePosition)
    }

    @Test
    fun `absoluteToRelative with activeVerses, sequential sectors, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val relativePosition = chapterRepresentation.absoluteToRelative(176400)

        // NOTE: they are the same value because the sectors are sequential
        // TODO: fix off by one
        Assert.assertEquals(176400, relativePosition)
    }

    // TODO: add test with non-sequential sectors

    @Test
    fun `relativeToAbsolute with relativeIdx at the start of the first node`() {
        val relativePosition = 500
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        // TODO: ask Joe if we want to have the relative position start at 0, or 1.
        //   If 0, then this is not correct, if 1, then this is correct
        Assert.assertEquals(499, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the first node`() {
        val relativePosition = 44100
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        // TODO: ask Joe if we want to have the relative position start at 0, or 1.
        //   If 0, then this is not correct, if 1, then this is correct
        Assert.assertEquals(44099, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in range of first node with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 1.5 seconds worth of frames
        val relativePosition = 66150

        // The first verseNode now contains 2 seconds worth of frame
        // Expected is 1389151 and not 1389150, becuase of exclusive ends

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        // TODO: ask Joe if we want to have the relative position start at 0, or 1.
        //   If 0, then this is not correct, if 1, then this is correct
        Assert.assertEquals(1389151, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350

        // The first verseNode now contains 2 seconds worth of frame
        // The + 13 is to account for the exclusive ends for each sector. We encounter 13 sectors while
        // in the calculation, so we add 13
        // TODO: ask Joe if we want to have the relative position start at 0, or 1.
        //   If 0, then this is not correct, if 1, then this is correct
        Assert.assertEquals(1653750 + 13, chapterRepresentation.relativeToAbsolute(relativePosition))
    }


    @Test
    fun `relativeToAbsolute with relativeIdx with non-sequential sectors and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350

        // The first verseNode now contains 2 seconds worth of frame
        // The + 13 is to account for the exclusive ends for each sector. We encounter 13 sectors while
        // in the calculation, so we add 13. The 44100*7 is to account for the unused frames added per verse node.
        // Since we add 44100 frames between each newly added sector, and we are adding one sector per verseNode, then
        // we need to offset by 44100*7
        // TODO: ask Joe if we want to have the relative position start at 0, or 1.
        //   If 0, then this is not correct, if 1, then this is correct
        val absolutePos = chapterRepresentation.relativeToAbsolute(relativePosition)
        Assert.assertEquals(1653750 + 13 + spaceBetweenSectors*7, absolutePos)
    }

    @Test
    fun `getRangeOfMarker with empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(1, 1, 0))

        Assert.assertNull(markerRange)
    }

    @Test
    fun `getRangeOfMarker with no matching verseMarker label`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(-1, 1, 0))

        Assert.assertNull(markerRange)
    }

    @Test
    fun `getRangeOfMarker with matching verseMarker label and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val verseNumber = 7
        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(verseNumber, verseNumber, 0))

        Assert.assertEquals(44100*(verseNumber - 1) until 44100*verseNumber, markerRange)
    }


    @Test
    fun `ChapterRepresentationConnection's framePosition with null start and end`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val framePosition = chapterRepresentation.ChapterRepresentationConnection(end = null).framePosition
        Assert.assertEquals(0, framePosition)
    }

    @Test
    fun `ChapterRepresentationConnection's framePosition with non-null start and end`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val framePosition = chapterRepresentation.ChapterRepresentationConnection(1000, 2000).framePosition
        Assert.assertEquals(1000, framePosition)
    }

    // TODO: add more test for framePosition once I know more about intended use

    @Test
    fun `ChapterRepresentationConnection's hasRemaining with null start and end, no scratchAudio recorded, and empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithoutAudio, chapter)

        val hasRemaining = chapterRepresentation.ChapterRepresentationConnection(end = null).hasRemaining()
        Assert.assertEquals(false, hasRemaining)
    }

    @Test
    fun `ChapterRepresentationConnection's hasRemaining with null start and end, no scratchAudio recorded, and non-empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val hasRemaining = chapterRepresentation.ChapterRepresentationConnection(end = null).hasRemaining()
        Assert.assertEquals(true, hasRemaining)
    }

    @Test
    fun `ChapterRepresentationConnection's hasRemaining with non-null start and end, no scratchAudio recorded, and empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithoutAudio, chapter)

        val hasRemaining = chapterRepresentation.ChapterRepresentationConnection(1000, 2000).hasRemaining()
        Assert.assertEquals(false, hasRemaining)
    }

    @Test
    fun `ChapterRepresentationConnection's hasRemaining with working audio, lockToVerse not equal to CHAPTER_UNLOCKED, and framePosition in locked verseNode`() {
        val verseIndexToLockTo = 5
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(verseIndexToLockTo)
        val hasRemaining = chapterRepresentationConnection.hasRemaining()

        Assert.assertTrue(hasRemaining)

    }


    fun readBytesFromFile(filePath: String): ByteArray? {
        try {
            val file = File(filePath)
            val inputStream = FileInputStream(file)
            val fileSize = file.length().toInt()
            val byteArray = ByteArray(fileSize)

            // Read the bytes from the file into the byteArray
            inputStream.read(byteArray)
            inputStream.close()

            return byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            println("Error reading bytes from file: ${e.message}")
            return null
        }
    }


    @Test
    fun `lockToVerse with null index`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = null)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }


    @Test
    fun `lockToVerse with index outside of activeVerses list`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = 10000)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }

    @Test
    fun `lockToVerse with negative index`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = -1)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }


    @Test
    fun `lockToVerse with index in range of activeVerses and position not in range of node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Check if the initial frame position is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)

        chapterRepresentationConnection.lockToVerse(5)

        // Check if the initial frame position is correct
        Assert.assertEquals(220500, chapterRepresentationConnection.framePosition)
    }



    @Test
    fun `getPcmBuffer with empty sectors and no scratchAudio`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)
        val byteArray = ByteArray(441000 * 2) { 1 }

        val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)

        Assert.assertEquals(0, bytesRead)

        val expectedBytesArray = ByteArray(441000 * 2) { 1 }
        Assert.assertArrayEquals(expectedBytesArray, byteArray)
    }



    @Test
    fun `getPcmBuffer with sequential sectors and reading the first verse`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, "${testDirWithAudio}\\chapter_narration.pcm")

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Gets the full first verse
        val byteArray = ByteArray(44100 * 2) { 1 }

        val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)

        Assert.assertEquals(44100 * 2, bytesRead)

        val expectedByteArray = ByteArray(44100 * 2)
        testAudioDataBuffer.get(expectedByteArray)
        Assert.assertArrayEquals(expectedByteArray, byteArray)

    }

    @Test
    fun `getPcmBuffer with sequential sectors and reading entire file`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, "${testDirWithAudio}\\chapter_narration.pcm")

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Gets the full 10 seconds of audio
        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        var totalBytesRead = 0
        while(chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for(i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }
        responseBuffer.position(0)

        val expectedBytesRead = 44100 * secondsOfAudio * 2
        Assert.assertEquals(expectedBytesRead, totalBytesRead)

        val expectedByteArray = ByteArray(44100 * secondsOfAudio * 2)
        testAudioDataBuffer.get(expectedByteArray)
        Assert.assertTrue(responseBuffer > testAudioDataBuffer)
    }

    @Test
    fun `getPcmBuffer with sequential sectors, 1 second of padding between verses, and reading entire audio file`() {
        val secondsOfAudio = 31
        val paddingLength = 44100
        // byteBuffer for 10 seconds of audio
        val buffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2 + paddingLength * 2 * numTestVerses)

        fillAudioBufferWithPadding(buffer, secondsOfAudio, paddingLength)
        writeByteBufferToPCMFile(buffer, "${testDirWithAudio}\\chapter_narration.pcm")

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses, 44100)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentation.onVersesUpdated()

        // Gets 10 seconds of audio
        val byteArray = ByteArray(secondsOfAudio*44100*2) { 1 }
        val responseBuffer = ByteBuffer.allocate(441000 * secondsOfAudio * 2)

        var totalBytesRead = 0
        while(chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for(i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }

        responseBuffer.position(0)

        Assert.assertEquals( 44100 * secondsOfAudio * 2 , totalBytesRead)

        for(i in 1 .. secondsOfAudio) {
            for(j in 1 .. 44100) {
                Assert.assertEquals(i.toShort(), responseBuffer.short)
            }
        }
    }

    @Test
    fun `getPcmBuffer with sequential sectors lockToVerse not equal to CHAPTER_UNLOCKED`() {
        val secondsOfAudio = 31
        val buffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(buffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(buffer, "${testDirWithAudio}\\chapter_narration.pcm")

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        // Locks to verse 6
        chapterRepresentationConnection.lockToVerse(5)

        var totalBytesRead = 0
        while(chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for(i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }
        responseBuffer.position(0)

        val expectedBytesRead = 44100 * 2
        Assert.assertEquals(expectedBytesRead, totalBytesRead)
        for(j in 1 .. 44100) {
            Assert.assertEquals(6.toShort(), responseBuffer.short)
        }
    }



    @Test
    fun `getPcmBuffer with sequential sectors lockToVerse equal to CHAPTER_UNLOCKED`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, "${testDirWithAudio}\\chapter_narration.pcm")

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection  = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        chapterRepresentationConnection.lockToVerse(index = null)

        var totalBytesRead = 0
        while(chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for(i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }
        responseBuffer.position(0)

        val expectedBytesRead = 44100 * 2 * 31
        Assert.assertEquals(expectedBytesRead, totalBytesRead)

        val expectedByteArray = ByteArray(44100 * secondsOfAudio * 2)
        testAudioDataBuffer.get(expectedByteArray)
        Assert.assertTrue(responseBuffer > testAudioDataBuffer)
    }
}





