package integrationtest

import com.jakewharton.rxrelay2.BehaviorRelay
import integrationtest.di.DaggerTestPersistenceComponent
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.pcm.PcmOutputStream
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.narration.testDataRootFilePath
import org.wycliffeassociates.otter.common.domain.narration.testDirWithAudio
import org.wycliffeassociates.otter.common.domain.narration.testDirWithoutAudio
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

class NarrationRenderingTest {

    private lateinit var chapter: Chapter
    private lateinit var workbookWithAudio: Workbook
    private lateinit var chunk: Observable<List<Chunk>>
    private val numTestVerses = 31

    private val seconds = 11
    private lateinit var file: File

    @Inject
    lateinit var narrationFactory: NarrationFactory
    lateinit var narration: Narration

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Before
    fun setup() {
        file = writeWavFile()

        createTestAudioFolders()
        workbookWithAudio = mockWorkbook(true)
        chapter = mockChapter()

        narration = narrationFactory.create(workbookWithAudio, chapter)
        println(narration.totalVerses)
    }

    @Test
    fun testNarration() {
        narration.audioReader.use {
            it.open()
            for (i in 1 until seconds) {
                val bytes = ByteArray(88200)
                it.getPcmBuffer(bytes)
                println(bytes.count { it.toInt() == i })
                Assert.assertTrue("Not all bytes matched for ${i}", bytes.all { it.toInt() == i })
            }
        }
    }

    private fun createTestAudioFolders() {
        val testProjectChapterDirWithAudio = "testProjectChapterDirWithAudio" // Replace with the desired folder name
        val withAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithAudio)

        try {
            Files.createDirectories(withAudioPath)

            val versesFile = File(withAudioPath.toFile(), "active_verses.json")
            val pcm = File(withAudioPath.toFile(), "chapter_narration.pcm")
            file.inputStream().use { ios -> pcm.outputStream().use { oos -> ios.transferTo(oos) } }
            pcm.deleteOnExit()
            versesFile.deleteOnExit()
            versesFile.outputStream().use { fos ->
                javaClass.classLoader.getResourceAsStream("active_verses.json").use { fis ->
                    fis.transferTo(fos)
                }
            }
            println(versesFile.length())
        } catch (e: Exception) {
            println("Failed to create test audio folders' at '$testDataRootFilePath': ${e.message}")
        }
    }

    private fun mockWorkbook(withAudio: Boolean): Workbook {
        val audioDirectory = if (withAudio) testDirWithAudio else testDirWithoutAudio
        return mockk<Workbook> {
            every { projectFilesAccessor.getChapterAudioDir(any(), any()) } returns audioDirectory
            every { source.slug } returns "gen"
        }
    }

    private var sortCount = 1
    private fun mockChapter(): Chapter {
        return mockk<Chapter> {
            every { chunks } returns BehaviorRelay.create<List<Chunk>>().also {
                it.accept(mockChunkList())
            }
            every { getSelectedTake() } returns null
            every { sort } returns sortCount++
        }
    }

    private fun mockChunkList(): List<Chunk> {
        return buildList {
            for (i in 0 until 10) {
                add(mockChunk(i - 2))
            }
        }
    }

    private fun mockChunk(number: Int): Chunk {
        return mockk<Chunk> {
            every { sort } returns number
            every { start } returns number
            every { end } returns number
        }
    }

    private fun createObservableChunkMock(chunk: Chunk): Observable<List<Chunk>> {
        return Observable.just(listOf( chunk))
    }

    private fun writeWavFile(): File {
        val file = File.createTempFile("test", ".pcm")
        file.deleteOnExit()

        PcmOutputStream(PcmFile(file)).use { wos ->
            for (i in 1..seconds) {
                for (x in 0 until 88200) {
                    wos.write(i)
                }
            }
        }

        return file
    }
}