package org.wycliffeassociates.otter.common.data.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.narration.ChapterRepresentation
import org.wycliffeassociates.otter.common.domain.narration.VerseNode
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import java.lang.IllegalStateException

class ChapterRepresentationTest {
    @JvmField
    @Rule
    val workingDir = TemporaryFolder()

    @JvmField
    @Rule
    val exceptionRule: ExpectedException = ExpectedException.none()

    private lateinit var workbook: Workbook
    private lateinit var chapter: Chapter

    private lateinit var chapterRepresentation: ChapterRepresentation

    private val verses = listOf(
        VerseNode(0, 1),
        VerseNode(1, 2),
        VerseNode(2, 3),
        VerseNode(3, 4)
    )

    private val audioData = byteArrayOf(0, 20, -3, 40, -50, 6, -77, 8)

    @Before
    fun setup() {
        workbook = mockWorkBook()
        chapter = mockChapter()

        chapterRepresentation = ChapterRepresentation(workbook, chapter)
        chapterRepresentation.workingAudio.file.outputStream().use {
            it.write(audioData)
        }
        chapterRepresentation.activeVerses.addAll(verses)
    }

    @Test
    fun testReadingEntirePcmBuffer() {
        val bytes = ByteArray(audioData.size)
        val read = chapterRepresentation.getPcmBuffer(bytes)

        Assert.assertEquals(audioData.size, read)
        Assert.assertArrayEquals(audioData, bytes)
        Assert.assertEquals(false, chapterRepresentation.hasRemaining())
    }

    @Test
    fun testReadingPartialPcmBuffer() {
        val expectedArray = audioData.sliceArray(0..4)

        val bytes = ByteArray(expectedArray.size)
        val read = chapterRepresentation.getPcmBuffer(bytes)

        Assert.assertEquals(expectedArray.size, read)
        Assert.assertArrayEquals(expectedArray, bytes)
        Assert.assertEquals(true, chapterRepresentation.hasRemaining())
    }

    @Test
    fun testReadingFailsWhenReaderClosed() {
        exceptionRule.expect(IllegalStateException::class.java)
        exceptionRule.expectMessage("getPcmBuffer called before opening file")

        val bytes = ByteArray(8)
        chapterRepresentation.close()
        chapterRepresentation.getPcmBuffer(bytes)
    }

    @Test
    fun testSeekChangesFramePosition() {
        chapterRepresentation.seek(2)

        Assert.assertEquals(2, chapterRepresentation.framePosition)
    }

    private fun mockWorkBook(): Workbook {
        return mockk<Workbook> {
            every { projectFilesAccessor } returns mockProjectFileAccessor()
        }
    }

    private fun mockChapter(): Chapter {
        return mockk<Chapter> {}
    }

    private fun mockProjectFileAccessor(): ProjectFilesAccessor {
        return mockk<ProjectFilesAccessor> {
            every { getChapterAudioDir(any(), any()) } returns workingDir.root
        }
    }
}