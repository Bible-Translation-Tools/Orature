package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths


val testDataRootFilePath: String = System.getProperty("user.home")
val testDirWithAudio = File(testDataRootFilePath, "testProjectChapterDirWithAudio")
val workingAudioFileWithAudio = File(testDirWithAudio, "chapter_narration.pcm")
val testDirWithoutAudio = File(testDataRootFilePath, "testProjectChapterDirWithoutAudio")

class ChapterRepresentationTest {

    lateinit var chapter: Chapter
    lateinit var workbookWithAudio: Workbook
    lateinit var workbookWithoutAudio: Workbook
    lateinit var chunk: Observable<Chunk>
    val numTestVerses = 31

    @Before
    fun setup() {
        createTestAudioFolders()
        chunk = createObservableChunkMock(mockChunk())
        workbookWithAudio = mockWorkbook(true)
        workbookWithoutAudio = mockWorkbook(false)
        chapter = mockChapter()
    }

    @After
    fun cleanup() {
        try {
            // Delete the test directories and their contents
            testDirWithAudio.deleteRecursively()
            testDirWithoutAudio.deleteRecursively()
        } catch (e: IOException) {
            println("Failed to delete test audio folders at '$testDataRootFilePath': ${e.message}")
        }
    }

    fun createTestAudioFolders() {

        val testProjectChapterDirWithAudio = "testProjectChapterDirWithAudio" // Replace with the desired folder name
        val testProjectChapterDirWithoutAudio =
            "testProjectChapterDirWithoutAudio" // Replace with the desired folder name

        val withAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithAudio)
        val withoutAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithoutAudio)

        try {
            Files.createDirectories(withAudioPath)
            Files.createDirectories(withoutAudioPath)
        } catch (e: Exception) {
            println("Failed to create test audio folders' at '$testDataRootFilePath': ${e.message}")
        }
    }

    fun mockWorkbook(withAudio: Boolean): Workbook {
        val audioDirectory = if (withAudio) testDirWithAudio else testDirWithoutAudio
        return mockk<Workbook> {
            every { projectFilesAccessor.getChapterAudioDir(any(), any()) } returns audioDirectory
        }
    }

    fun mockChapter(): Chapter {
        return mockk<Chapter> {
            every { getDraft() } returns chunk
        }
    }

    fun mockChunk(): Chunk {
        return mockk<Chunk> {
            every { start } returns 0
            every { end } returns 0
        }
    }

    private fun createObservableChunkMock(chunk: Chunk): Observable<Chunk> {
        return Observable.just(chunk)
    }

    // Initializes each verse with placed equal to true and with one sector that holds one second worth of frames.
    // where the start of each added sector is offset by "paddingLength" number of frames
    private fun initializeVerseNodeList(verseNodeList: MutableList<VerseNode>, paddingLength: Int = 0) {
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

    // Simulates re-recording each verse for some number of frames (sectorLength), with some amount of "dead-space"
    // (spaceBetweenSectors) between each newly added sector
    private fun addSectorsToEnd(verseNodeList: MutableList<VerseNode>, sectorLength: Int, spaceBetweenSectors: Int) {
        var lastSectorEnd = verseNodeList.last().sectors.last().last + 1
        for (i in 1 until verseNodeList.size) {
            val start = lastSectorEnd + spaceBetweenSectors
            val end = start + sectorLength
            lastSectorEnd = end
            verseNodeList[i].sectors.add(start until end)
        }
    }

    private fun writeByteBufferToPCMFile(byteBuffer: ByteBuffer, PCMFile: File) {
        try {
            val byteArray = ByteArray(byteBuffer.remaining())
            byteBuffer.get(byteArray)

            val fos = FileOutputStream(PCMFile)

            fos.write(byteArray)

            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        byteBuffer.rewind()
    }


    fun fillAudioBufferWithPadding(byteBuffer: ByteBuffer, secondsOfAudio: Int, paddingLength: Int) {
        for (i in 1..secondsOfAudio) {
            for (j in 1..44100) {
                byteBuffer.putShort(i.toShort())
            }
            for (j in 1..paddingLength) {
                byteBuffer.putShort(0)
            }
        }
        byteBuffer.rewind()
    }


    @Test
    fun `totalFrames with initialized totalVerses list`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        Assert.assertEquals(44100 * numTestVerses, chapterRepresentation.totalFrames)
    }

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
        Assert.assertEquals(176400, relativePosition)
    }

    @Test
    fun `absoluteToRelative with activeVerses, sequential sectors with padding between each verseNode, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses, 44100)

        val relativePosition = chapterRepresentation.absoluteToRelative(88200)

        // NOTE: we expect 44100, because the frame 88200 is the start of the second frame in first verse node.
        // so relatively, it is the frame at index 44100.
        Assert.assertEquals(44100, relativePosition)
    }

    @Test
    fun `absoluteToRelative with activeVerses, non-sequential sectors, padding between sectors, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses, 44100)
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = chapterRepresentation.absoluteToRelative(2690100)

        // NOTE: we expect 44100, because the frame 2690100 is the start of the second frame in first verse node.
        // so relatively, it is the frame at index 44100.
        Assert.assertEquals(44100, relativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the start of the first node`() {
        val relativePosition = 500
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        Assert.assertEquals(500, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the first node`() {
        val relativePosition = 44099
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        Assert.assertEquals(44099, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the start of the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100 * 2 + 31
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100 * 2 - 1
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in range of first node, with non-sequential sectors, and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 1.5 seconds worth of frames
        val relativePosition = 66150

        val absolutePosition = chapterRepresentation.relativeToAbsolute(relativePosition)
        val expectedAbsolutePosition = 1389150
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeToAbsolute with relativeIdx at the start of the second node, with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = 44100 * 2

        val absolutePosition = chapterRepresentation.relativeToAbsolute(relativePosition)
        val expectedAbsolutePosition = 44100
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the second node, with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = 44100 * 4 - 1

        val absolutePosition = chapterRepresentation.relativeToAbsolute(relativePosition)
        val expectedAbsolutePosition = 1455299
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in range of 7th node, with non-sequential sectors, and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350
        val absolutePosition = chapterRepresentation.relativeToAbsolute(relativePosition)
        val expectedAbsolutePosition = 1653750
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the second node, with non-sequential sectors, and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 44100 * 4 - 1

        val expectedAbsolutePos = 1543499
        val absolutePos = chapterRepresentation.relativeToAbsolute(relativePosition)
        Assert.assertEquals(expectedAbsolutePos, absolutePos)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in range of 7th node, with non-sequential sectors, and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350

        val expectedAbsolutePos = 1653750 + spaceBetweenSectors * 7
        val absolutePos = chapterRepresentation.relativeToAbsolute(relativePosition)
        Assert.assertEquals(expectedAbsolutePos, absolutePos)
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

        Assert.assertEquals(44100 * (verseNumber - 1) until 44100 * verseNumber, markerRange)
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
        Assert.assertEquals(0, framePosition)
    }

    @Test
    fun `ChapterRepresentationConnection's absoluteFramePosition with null start and end`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val absoluteFramePosition =
            chapterRepresentation.ChapterRepresentationConnection(end = null).absoluteFramePosition
        Assert.assertEquals(0, absoluteFramePosition)
    }

    @Test
    fun `ChapterRepresentationConnection's absoluteFramePosition with non-null start and end`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val absoluteFramePosition =
            chapterRepresentation.ChapterRepresentationConnection(1000, 2000).absoluteFramePosition
        Assert.assertEquals(1000, absoluteFramePosition)
    }


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


    @Test
    fun `ChapterRepresentationConnection's lockToVerse with null index`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = null)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }


    @Test
    fun `ChapterRepresentationConnection's lockToVerse with index outside of activeVerses list`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = 10000)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }


    @Test
    fun `ChapterRepresentationConnection's lockToVerse with negative index`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentationConnection.lockToVerse(index = -1)

        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
    }


    @Test
    fun `ChapterRepresentationConnection's lockToVerse with index in range of activeVerses and position not in range of node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Check if the initial frame position is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)

        Assert.assertEquals(0, chapterRepresentationConnection.absoluteFramePosition)

        chapterRepresentationConnection.lockToVerse(5)

        // Verify that the framePosition is in the relative verse space
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)

        // Verify that the absoluteFrame position is in the absolute chapter space
        Assert.assertEquals(
            chapterRepresentation.activeVerses[5].firstFrame(),
            chapterRepresentationConnection.absoluteFramePosition
        )

    }


    @Test
    fun `ChapterRepresentationConnection's seek with sample in range of relative chapter space and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Check if the initial frame positions is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(0, chapterRepresentationConnection.absoluteFramePosition)

        // A random sample in the relative chapter space
        val sample = 82000

        chapterRepresentationConnection.seek(sample)

        // Check if positions are correct. In this case they are the same due to sequential sectors
        Assert.assertEquals(sample, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(sample, chapterRepresentationConnection.absoluteFramePosition)
    }

    @Test
    fun `ChapterRepresentationConnection's seek with sample in range of relative chapter space, empty spaces, and non-sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses, 44100)
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)


        // Check if the initial frame positions is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(0, chapterRepresentationConnection.absoluteFramePosition)

        // Sample corresponding to the middle of verse 5
        val sample = 44100 * 9

        chapterRepresentationConnection.seek(sample)

        // Check if positions are correct. In this case they are the same due to sequential sectors
        Assert.assertEquals(sample, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(
            chapterRepresentation.activeVerses[4].sectors[1].first,
            chapterRepresentationConnection.absoluteFramePosition
        )
    }


    @Test
    fun `ChapterRepresentationConnection's seek, locking to verse, with sample in relative verse space, and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Check if the initial frame positions is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(0, chapterRepresentationConnection.absoluteFramePosition)

        val verseIndexToLockTo = 4
        chapterRepresentationConnection.lockToVerse(verseIndexToLockTo)


        // A random sample in the relative chapter space
        val sample = 500

        chapterRepresentationConnection.seek(sample)

        // Check if positions are correct. In this case they are the same due to sequential sectors
        Assert.assertEquals(sample, chapterRepresentationConnection.framePosition)
        Assert.assertEquals(
            chapterRepresentation.activeVerses[verseIndexToLockTo].firstFrame() + sample,
            chapterRepresentationConnection.absoluteFramePosition
        )
    }

    @Test
    fun `ChapterRepresentationConnection's getPcmBuffer with empty sectors and no scratchAudio`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)
        val byteArray = ByteArray(441000 * 2) { 1 }

        val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)

        Assert.assertEquals(0, bytesRead)

        val expectedBytesArray = ByteArray(441000 * 2) { 1 }
        Assert.assertArrayEquals(expectedBytesArray, byteArray)
    }


    @Test
    fun `ChapterRepresentationConnection's getPcmBuffer with sequential sectors and reading the first verse`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, workingAudioFileWithAudio)

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Gets the full first verse
        val byteArray = ByteArray(44100 * 2) { 1 }

        val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)

        Assert.assertEquals(44100 * 2, bytesRead)

        val expectedByteArray = ByteArray(44100 * 2)
        testAudioDataBuffer.get(expectedByteArray)
        Assert.assertArrayEquals(expectedByteArray, byteArray)

    }

    @Test
    fun `ChapterRepresentationConnection's getPcmBuffer with sequential sectors and reading entire file`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, workingAudioFileWithAudio)

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        var totalBytesRead = 0
        while (chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for (i in 0 until bytesRead) {
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
    fun `ChapterRepresentationConnection's getPcmBuffer with sequential sectors, 1 second of padding between verses, and reading entire audio file`() {
        val secondsOfAudio = 31
        val paddingLength = 44100
        // byteBuffer for 10 seconds of audio
        val buffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2 + paddingLength * 2 * numTestVerses)

        fillAudioBufferWithPadding(buffer, secondsOfAudio, paddingLength)
        writeByteBufferToPCMFile(buffer, workingAudioFileWithAudio)

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses, 44100)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        chapterRepresentation.onVersesUpdated()

        val byteArray = ByteArray(secondsOfAudio * 44100 * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(441000 * secondsOfAudio * 2)

        var totalBytesRead = 0
        while (chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for (i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }

        responseBuffer.position(0)

        Assert.assertEquals(44100 * secondsOfAudio * 2, totalBytesRead)

        for (i in 1..secondsOfAudio) {
            for (j in 1..44100) {
                Assert.assertEquals(i.toShort(), responseBuffer.short)
            }
        }
    }

    @Test
    fun `ChapterRepresentationConnection's getPcmBuffer with sequential sectors lockToVerse not equal to CHAPTER_UNLOCKED`() {
        val secondsOfAudio = 31
        val buffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(buffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(buffer, workingAudioFileWithAudio)

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        // Locks to verse 6
        chapterRepresentationConnection.lockToVerse(5)

        var totalBytesRead = 0
        while (chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for (i in 0 until bytesRead) {
                responseBuffer.put(byteArray[i])
            }
        }
        responseBuffer.position(0)

        val expectedBytesRead = 44100 * 2
        Assert.assertEquals(expectedBytesRead, totalBytesRead)
        for (j in 1..44100) {
            Assert.assertEquals(6.toShort(), responseBuffer.short)
        }
    }


    @Test
    fun `ChapterRepresentationConnection's getPcmBuffer with sequential sectors lockToVerse equal to CHAPTER_UNLOCKED`() {
        val secondsOfAudio = 31
        val testAudioDataBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        fillAudioBufferWithPadding(testAudioDataBuffer, secondsOfAudio, 0)
        writeByteBufferToPCMFile(testAudioDataBuffer, workingAudioFileWithAudio)

        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val byteArray = ByteArray(44100 * secondsOfAudio * 2) { 1 }
        val responseBuffer = ByteBuffer.allocate(44100 * secondsOfAudio * 2)

        chapterRepresentationConnection.lockToVerse(index = null)

        var totalBytesRead = 0
        while (chapterRepresentationConnection.hasRemaining()) {
            val bytesRead = chapterRepresentationConnection.getPcmBuffer(byteArray)
            totalBytesRead += bytesRead
            for (i in 0 until bytesRead) {
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





