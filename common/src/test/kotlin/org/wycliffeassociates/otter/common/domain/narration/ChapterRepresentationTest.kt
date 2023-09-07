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

class ChapterRepresentationTest {

    lateinit var chapter: Chapter
    lateinit var workbook: Workbook
    lateinit var chunk: Observable<Chunk>
    val numTestVerses = 31


    @Before
    fun setup() {
        chunk = createObservableChunkMock(mockChunk())
        workbook = mockWorkbook()
        chapter = mockChapter()
    }

    fun mockWorkbook() : Workbook {
        return mockk<Workbook>{
            every { projectFilesAccessor.getChapterAudioDir(any(), any())} returns File("C:\\Users\\hilld\\testProjectChapterDir")
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

    // Initializes each verse with placed equal to true and with one sector
    // that is an int range of 44100*i until 44100*(i+1)
    // so each verseNode will have one second of recording
    fun initializeVerseNodeList(verseNodeList : MutableList<VerseNode>) {
        for (i in 0 until numTestVerses) {
            val verseMarker = VerseMarker((i + 1), (i + 1), 0)
            val sectors = mutableListOf<IntRange>()
            val verseNode = VerseNode(0, 0, true, verseMarker, sectors)
            sectors.add(44100 * i until (44100 * (i + 1)))
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

    @Test
    fun `totalFrames with initialized totalVerses list`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        Assert.assertEquals(44100*numTestVerses - numTestVerses, chapterRepresentation.totalFrames)
    }


    @Test
    fun `relativeToAbsolute with relativeIdx at the start of the first node`() {
        val relativePosition = 500
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        Assert.assertEquals(500, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx at the end of the first node`() {
        val relativePosition = 44099
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val absolutePositionFromRelativePosition = chapterRepresentation.relativeToAbsolute(relativePosition)

        Assert.assertEquals(44099, absolutePositionFromRelativePosition)
    }

    @Test
    fun `relativeToAbsolute with relativeIdx in range of first node with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 1.5 seconds worth of frames
        val relativePosition = 66150

        // The first verseNode now contains 2 seconds worth of frame
        // Expected is 1389151 and not 1389150, becuase of exclusive ends
        Assert.assertEquals(1389151, chapterRepresentation.relativeToAbsolute(relativePosition))
    }

    @Test
    fun `relativeToAbsolute with relativeIdx with non-sequential sectors and no unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, 0)

        // sets relative position to 13.5 seconds worth of frames
        val relativePosition = 595350

        // The first verseNode now contains 2 seconds worth of frame
        // The + 13 is to account for the exclusive ends for each sector. We encounter 13 sectors while
        // in the calculation, so we add 13
        Assert.assertEquals(1653750 + 13, chapterRepresentation.relativeToAbsolute(relativePosition))
    }


    @Test
    fun `relativeToAbsolute with relativeIdx with non-sequential sectors and unused frames`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
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
        Assert.assertEquals(1653750 + 13 + spaceBetweenSectors*7, chapterRepresentation.relativeToAbsolute(relativePosition))
    }

    @Test
    fun `getRangeOfMarker with empty activeVerses`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)

        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(1, 1, 0))

        Assert.assertNull(markerRange)
    }

    @Test
    fun `getRangeOfMarker with no matching verseMarker label`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(-1, 1, 0))

        Assert.assertNull(markerRange)
    }

    @Test
    fun `getRangeOfMarker with matching verseMarker label and sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        val verseNumber = 7
        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(verseNumber, verseNumber, 0))

        Assert.assertEquals(44100*(verseNumber - 1) until 44100*verseNumber, markerRange)
    }

    @Test
    fun `getRangeOfMarker with matching verseMarker label and non-sequential sectors`() {
        val chapterRepresentation = ChapterRepresentation(workbook, chapter)
        initializeVerseNodeList(chapterRepresentation.totalVerses)

        // 44100 frames are added between newly added sectors, as specified by the value for spaceBetweenSectors
        val spaceBetweenSectors = 44100
        addSectorsToEnd(chapterRepresentation.totalVerses, 44100, spaceBetweenSectors)

        val verseNumber = 7
        val markerRange = chapterRepresentation.getRangeOfMarker(VerseMarker(verseNumber, verseNumber, 0))

        // TODO: fix once I get clarification about intended functionality of this
        Assert.assertFalse(true)
    }


}





