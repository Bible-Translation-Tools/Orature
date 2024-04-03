package integrationtest

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import integrationtest.di.DaggerTestPersistenceComponent
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.pcm.PcmOutputStream
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.narration.ActiveRecordingDrawable
import org.wycliffeassociates.otter.common.domain.narration.AudioScene
import org.wycliffeassociates.otter.common.domain.narration.Narration
import org.wycliffeassociates.otter.common.domain.narration.NarrationFactory
import org.wycliffeassociates.otter.common.domain.narration.testDataRootFilePath
import org.wycliffeassociates.otter.common.domain.narration.testDirWithAudio
import org.wycliffeassociates.otter.common.domain.narration.testDirWithoutAudio
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform.NarrationWaveformRenderer
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
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
    }

    @Test
    fun `test reading verse by verse gets correct content`() {
        narration.audioReader.use {
            it.open()
            for (i in 1 until seconds) {
                val bytes = ByteArray(88200)
                it.getPcmBuffer(bytes)
                Assert.assertTrue(
                    "Not all bytes matched for ${i}; found: ${bytes.firstOrNull { it.toInt() != i }}",
                    bytes.all { it.toInt() == i }
                )
            }
        }
    }

    @Test
    fun `test reading a half verse at a time gets correct content`() {
        narration.audioReader.use {
            it.open()
            for (i in 1 until seconds) {
                for (j in 1..2) {
                    val bytes = ByteArray(44100)
                    it.getPcmBuffer(bytes)
                    Assert.assertTrue(
                        "Not all bytes matched for ${i}; found: ${bytes.firstOrNull { it.toInt() != i }}",
                        bytes.all { it.toInt() == i }
                    )
                }
            }
        }
    }

    @Test
    fun `test repeated frames generated at the same audio location are equal`() {
        val rendererWidth = 1920
        val rendererHeight = 1080
        narration.audioReader.use { narrationReader ->
            narrationReader.open()
            val recordingOff = ReplayRelay.create<Boolean>()
            recordingOff.accept(false)
            val scene = AudioScene(
                narrationReader,
                ReplayRelay.create(),
                recordingOff,
                rendererWidth,
                10,
                44100,
                activeDrawable = mockk<ActiveRecordingDrawable> {
                    every { hasData() } returns false
                }
            )
            val renderer = NarrationWaveformRenderer(scene, rendererWidth, rendererHeight)
            val frames = renderFrames(renderer, rendererWidth, rendererHeight, 0, 10)
            
            compareImages(frames)
            writeFramesToImages(frames)
        }
    }

    private fun renderFrames(
        renderer: NarrationWaveformRenderer,
        rendererWidth: Int,
        rendererHeight: Int,
        locationToRender: Int,
        framesToRender: Int
    ): List<Image> {
        val frames = arrayListOf<Image>()
        for (i in 0 until framesToRender) {
            val image = WritableImage(rendererWidth, rendererHeight)
            val (frame, _) = renderer.generateImage(
                locationToRender,
                rendererHeight.toDouble(),
                image,
                null,
                null
            )
            frames.add(frame)
        }
        return frames
    }

    private fun compareImages(frames: List<Image>) {
        if (frames.isEmpty()) return

        for (x in 0 until frames[0]!!.width.toInt()) {
            for (y in 0 until frames[0]!!.height.toInt()) {
                val pixel = frames.map { it.pixelReader.getArgb(x, y) }
                Assert.assertTrue(
                    "Not all pixels at ($x, $y) are the same, should be ${pixel[0]}",
                    pixel.all { it == pixel[0] })
            }
        }
    }

    private fun writeFramesToImages(frames: List<Image>) {
        frames.forEachIndexed { index, image ->
            val bImage: BufferedImage = SwingFXUtils.fromFXImage(image, null)
            ImageIO.write(bImage, "png", File("${index}.png"))
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
        } catch (e: Exception) {
            System.err.println("Failed to create test audio folders' at '$testDataRootFilePath': ${e.message}")
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
            for (i in -2..8) {
                if (i != 0) add(mockChunk(i))
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
        return Observable.just(listOf(chunk))
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