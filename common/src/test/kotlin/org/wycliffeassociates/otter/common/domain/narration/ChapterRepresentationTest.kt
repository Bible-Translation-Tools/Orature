/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.narration

import com.jakewharton.rxrelay2.BehaviorRelay
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
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
            every { source.slug } returns "gen"
        }
    }

    var sortCount = 1
    fun mockChapter(): Chapter {
        return mockk<Chapter> {
            every { getDraft() } returns chunk
            every { chunks } returns BehaviorRelay.create<List<Chunk>>().also { it.accept(emptyList<Chunk>()) }
            every { sort } returns sortCount++
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
    val framesPerVerse = 44100
    private fun initializeVerseMarkersWithSectors(verseNodeList: MutableList<VerseNode>, paddingLength: Int = 0) {
        var start = -1
        val numTitles = getNumberOfTitles(verseNodeList)
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(true, verseMarker, sectors)
            sectors.add(start + 1..start + framesPerVerse)
            start += framesPerVerse + paddingLength
            verseNodeList.add(i + numTitles, verseNode)
        }
    }

    private fun initializeTitlesWithSectors(verseNodeList: MutableList<VerseNode>, paddingLength: Int = 0) {
        val titles = getOrInsertTitleMarkers(verseNodeList)

        var start = verseNodeList.lastOrNull()?.lastIndex()?.let { it + 1 } ?: 0
        titles.forEach {
            it.sectors.add(start..(start + framesPerVerse))
            start += framesPerVerse + 1
            it.placed = true
        }
    }

    private fun getOrInsertTitleMarkers(verseNodeList: MutableList<VerseNode>): List<VerseNode> {
        val regex = """orature-vm-\d+""".toRegex()
        return verseNodeList.filter {
            !regex.containsMatchIn(it.marker.formattedLabel)
        }.let { titles ->
            titles.ifEmpty {
                val bookMarker = BookMarker(workbookWithAudio.source.slug, 0)
                val bookNode = VerseNode(true, bookMarker)
                bookNode.sectors.add(0 .. framesPerVerse)

                val chapterMarker = ChapterMarker(1, 0)
                val chapterNode = VerseNode(true, chapterMarker)
                chapterNode.sectors.add(framesPerVerse + 1 .. framesPerVerse * 2)

                verseNodeList.add(0, bookNode)
                verseNodeList.add(1, chapterNode)
                listOf(bookNode, chapterNode)
            }
        }
    }

    // Simulates re-recording each verse for some number of frames (sectorLength), with some amount of "dead-space"
    // (spaceBetweenSectors) between each newly added sector
    private fun addSectorsToEnd(verseNodeList: MutableList<VerseNode>, sectorLength: Int, spaceBetweenSectors: Int) {
        var lastSectorEnd = verseNodeList.last().sectors.last().last + 1
        val numTitles = getNumberOfTitles(verseNodeList)
        for (i in 0 until numTestVerses) {
            val start = lastSectorEnd + spaceBetweenSectors
            val end = start + sectorLength
            lastSectorEnd = end
            verseNodeList[i + numTitles].sectors.add(start until end)
        }
    }

    // Returns the number of titles present in the verseNodeList
    private fun getNumberOfTitles(verseNodeList: MutableList<VerseNode>): Int {
        var numTitles = 0
        verseNodeList.forEach {
            if (!it.marker.formattedLabel.matches(Regex("orature-vm-\\d*"))) {
                numTitles++
            }
        }
        return numTitles
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        Assert.assertEquals(44100 * numTestVerses, chapterRepresentation.totalFrames)
    }

    @Test
    fun `versesWithRecordings with no markers recorded`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val verses = chapterRepresentation.versesWithRecordings()

        // Verify that verses is of the correct length
        Assert.assertEquals(verses.size, chapterRepresentation.totalVerses.size)

        // Verify that no verses have recordings
        Assert.assertFalse(verses.contains(true))
    }

    @Test
    fun `versesWithRecordings with all markers recorded`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        initializeTitlesWithSectors(chapterRepresentation.totalVerses)

        val verses = chapterRepresentation.versesWithRecordings()

        // Verify that verses is of the correct length
        Assert.assertEquals(verses.size, chapterRepresentation.totalVerses.size)

        // Verify that all markers have some recordings
        Assert.assertFalse(verses.contains(false))
    }

    @Test
    fun `versesWithRecordings with only titles recorded`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeTitlesWithSectors(chapterRepresentation.totalVerses)

        val verses = chapterRepresentation.versesWithRecordings()

        val titles = getOrInsertTitleMarkers(chapterRepresentation.totalVerses)

        // Verify that only title markers have recordings
        chapterRepresentation.totalVerses.forEachIndexed { idx, verseNode ->
            if (titles.contains(verseNode)) {
                Assert.assertTrue(verses[idx])
            } else {
                Assert.assertFalse(verses[idx])
            }
        }
    }

    @Test
    fun `versesWithRecordings all titles recorded and some verses recorded`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        initializeTitlesWithSectors(chapterRepresentation.totalVerses)

        // Removes recording data from the end of the verse node list
        val numVersesToRemove = 3
        chapterRepresentation.apply {
            for (i in totalVerses.size - numVersesToRemove until totalVerses.size) {
                totalVerses[i].apply {
                    sectors.clear()
                    placed = false
                }
            }
        }

        val verses = chapterRepresentation.versesWithRecordings()

        // Verify that we have numTestVerses - versesToRemove verses recorded
        val numTitles = getOrInsertTitleMarkers(chapterRepresentation.totalVerses).size
        val (recorded, notRecorded) = verses.partition { it }
        Assert.assertEquals(recorded.size, numTestVerses - numVersesToRemove + numTitles)
        Assert.assertEquals(notRecorded.size, numVersesToRemove)
    }

    @Test
    fun `versesWithRecordings with markers that have recordings, but are not placed`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        initializeTitlesWithSectors(chapterRepresentation.totalVerses)

        // Sets placed to false for the last numVersesToRemove verses
        val numVersesToRemove = 3
        chapterRepresentation.apply {
            for (i in totalVerses.size - numVersesToRemove until totalVerses.size) {
                totalVerses[i].apply {
                    placed = false
                }
            }
        }

        val verses = chapterRepresentation.versesWithRecordings()
        val numTitles = getOrInsertTitleMarkers(chapterRepresentation.totalVerses).size
        val (recorded, notRecorded) = verses.partition { it }
        Assert.assertEquals(recorded.size, numTestVerses - numVersesToRemove + numTitles)
        Assert.assertEquals(notRecorded.size, numVersesToRemove)
    }

    @Test
    fun `audioLocationToLocationInChapter with empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)

        val relativePosition = chapterRepresentation.absoluteFrameToRelativeChapterFrame(1000)
        Assert.assertEquals(0, relativePosition)
    }

    @Test
    fun `audioLocationToLocationInChapter with non-empty activeVerses and absoluteFrame not in activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val relativePosition = chapterRepresentation.absoluteFrameToRelativeChapterFrame(-5)
        Assert.assertEquals(0, relativePosition)
    }

    @Test
    fun `audioLocationToLocationInChapter with activeVerses, sequential sectors, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val relativePosition = chapterRepresentation.absoluteFrameToRelativeChapterFrame(176400)

        // NOTE: they are the same value because the sectors are sequential
        Assert.assertEquals(176400, relativePosition)
    }

    @Test
    fun `audioLocationToLocationInChapter with activeVerses, sequential sectors with padding between each verseNode, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, 44100)

        val relativePosition = chapterRepresentation.absoluteFrameToRelativeChapterFrame(88200)

        // NOTE: we expect 44100, because the frame 88200 is the start of the second frame in first verse node.
        // so relatively, it is the frame at index 44100.
        Assert.assertEquals(44100, relativePosition)
    }

    @Test
    fun `audioLocationToLocationInChapter with activeVerses, non-sequential sectors, padding between sectors, and non-null verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, 44100)
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = chapterRepresentation.absoluteFrameToRelativeChapterFrame(2690100)

        // NOTE: we expect 44100, because the frame 2690100 is the start of the second frame in first verse node.
        // so relatively, it is the frame at index 44100.
        Assert.assertEquals(44100, relativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the start of the first node`() {
        val relativePosition = 500
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)

        Assert.assertEquals(500, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the end of the first node`() {
        val relativePosition = 44099
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)

        Assert.assertEquals(44099, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the start of the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx in the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100 * 2 + 31
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the end of the second node`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val secondNodesStart = 44100 * 2 - 1
        val absolutePositionFromRelativePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(secondNodesStart)

        Assert.assertEquals(secondNodesStart, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx in range of first node, with non-sequential sectors, and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 1.5 seconds worth of frames
        val relativePosition = 66150

        val absolutePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
        val expectedAbsolutePosition = 1389150
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the start of the second node, with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = 44100 * 2

        val absolutePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
        val expectedAbsolutePosition = 44100
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the end of the second node, with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        val relativePosition = 44100 * 4 - 1

        val absolutePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
        val expectedAbsolutePosition = 1455299
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx in range of 7th node, with non-sequential sectors, and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350
        val absolutePosition = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
        val expectedAbsolutePosition = 1653750
        Assert.assertEquals(expectedAbsolutePosition, absolutePosition)
    }


    @Test
    fun `relativeChapterToAbsolute with relativeIdx at the end of the second node, with non-sequential sectors, and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 44100 * 4 - 1

        val expectedAbsolutePos = 1543499
        val absolutePos = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
        Assert.assertEquals(expectedAbsolutePos, absolutePos)
    }

    @Test
    fun `relativeChapterToAbsolute with relativeIdx in range of 7th node, with non-sequential sectors, and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350

        val expectedAbsolutePos = 1653750 + spaceBetweenSectors * 7
        val absolutePos = chapterRepresentation.relativeChapterFrameToAbsoluteIndex(relativePosition)
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(-1, 1, 0))

        Assert.assertNull(markerRange)
    }

    @Test
    fun `getRangeOfMarker with matching verseMarker label and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

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
    fun `absoluteToRelativeVerse with absoluteFrame in range of verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val verseIndex = 3
        val offsetIntoVerse = 500
        val absoluteFrame = chapterRepresentation.activeVerses[verseIndex].firstIndex() + offsetIntoVerse
        val actualRelativeVersePosition = chapterRepresentationConnection
            .absoluteFrameToRelativeVerseFrame(absoluteFrame, verseIndex)
        // Verify that the relativeVerse position is equal to the number of frames from the first frame, to the
        // specified absolute position
        Assert.assertEquals(offsetIntoVerse, actualRelativeVersePosition)
    }

    @Test
    fun `absoluteToRelativeVerse with absoluteFrame not in range of verse`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        val verseIndex = 3
        val absoluteFrame = chapterRepresentation.activeVerses[verseIndex].lastIndex() + 500

        try {
            chapterRepresentationConnection.absoluteFrameToRelativeVerseFrame(absoluteFrame, verseIndex)
            Assert.fail("Error: expecting exception")
        } catch (indexOutOfBoundsException: IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }

    @Test
    fun `absoluteToRelative with lockToVerse equal to CHAPTER_UNLOCKED and padding between verse sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val paddingBetweenVerses = 1000
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, paddingBetweenVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Sets the absolute frame to the beginning of verse specified by verseIndex
        val verseIndex = 3
        val absoluteFrame = framesPerVerse * verseIndex + paddingBetweenVerses * verseIndex

        val actualRelativePos = chapterRepresentationConnection.absoluteFrameToRelativeFrame(absoluteFrame)
        val expectedRelativePos = framesPerVerse * verseIndex
        Assert.assertEquals(expectedRelativePos, actualRelativePos)
    }

    @Test
    fun `absoluteToRelative with lockToVerse not equal to CHAPTER_UNLOCKED and padding between verse sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        val paddingBetweenVerses = 1000
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, paddingBetweenVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Sets the absolute frame to the beginning of verse specified by verseIndex
        val verseIndex = 3
        val absoluteFrame = framesPerVerse * verseIndex + paddingBetweenVerses * verseIndex

        // Locks to verse specified by verseIndex
        chapterRepresentationConnection.lockToVerse(verseIndex)

        val actualRelativePos = chapterRepresentationConnection.absoluteFrameToRelativeFrame(absoluteFrame)
        val expectedRelativePos = absoluteFrame - chapterRepresentation.activeVerses[verseIndex].firstIndex()
        Assert.assertEquals(expectedRelativePos, actualRelativePos)
    }
    // TODO: add test for absoluteToRelative


    @Test
    fun `ChapterRepresentationConnection's hasRemaining with null start and end, no scratchAudio recorded, and empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithoutAudio, chapter)

        val hasRemaining = chapterRepresentation.ChapterRepresentationConnection(end = null).hasRemaining()
        Assert.assertEquals(false, hasRemaining)
    }

    @Test
    fun `ChapterRepresentationConnection's hasRemaining with null start and end, no scratchAudio recorded, and non-empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)

        val hasRemaining = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }.hasRemaining()
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Check if the initial frame position is correct
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)

        Assert.assertEquals(0, chapterRepresentationConnection.absoluteFramePosition)

        chapterRepresentationConnection.lockToVerse(5)

        // Verify that the framePosition is in the relative verse space
        Assert.assertEquals(0, chapterRepresentationConnection.framePosition)

        // Verify that the absoluteFrame position is in the absolute chapter space
        Assert.assertEquals(
            chapterRepresentation.activeVerses[5].firstIndex(),
            chapterRepresentationConnection.absoluteFramePosition
        )

    }

    @Test
    fun `ChapterRepresentationConnection's relativeVerseToRelativeChapter`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null)

        // Specifies what verse and what frame from the start of that verse to test
        val verseIndex = 3
        val sampleInVerseSpace = 500

        // Verify that the verse is starting at the expected actual position
        Assert.assertEquals(framesPerVerse * verseIndex, chapterRepresentation.activeVerses[verseIndex].firstIndex())

        // Verify that the given relativeVerse position maps to the correct relativeChapter location
        val expectedRelativeChapterPos = sampleInVerseSpace + framesPerVerse * verseIndex
        val actualRelativeChapterPos = chapterRepresentationConnection
            .frameInVerseToFrameInChapter(sampleInVerseSpace, verseIndex)

        Assert.assertEquals(expectedRelativeChapterPos, actualRelativeChapterPos)
    }

    @Test
    fun `ChapterRepresentationConnection's seek with sample in range of relative chapter space and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbookWithAudio, chapter)
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, 44100)
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
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
            chapterRepresentation.activeVerses[verseIndexToLockTo].firstIndex() + sample,
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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses, 44100)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
        initializeVerseMarkersWithSectors(chapterRepresentation.totalVerses)
        val chapterRepresentationConnection = chapterRepresentation.ChapterRepresentationConnection(end = null).apply {
            open()
        }

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
