package org.wycliffeassociates.otter.common.domain.narration
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import java.io.File




class NarrationTest {
    val testDataRootFilePath = "/Users"
    val testDirWithAudio =  "${testDataRootFilePath}/testProjectChapterDirWithAudio"
    val testDirWithoutAudio = "${testDataRootFilePath}/testProjectChapterDirWithoutAudio"

    lateinit var workbook: Workbook
    lateinit var chapter: Chapter
    lateinit var player: IAudioPlayer
    lateinit var recorder: IAudioRecorder
    lateinit var audioFileUtils: AudioFileUtils
    lateinit var splitAudioOnCues: SplitAudioOnCues
    lateinit var chunk: Observable<Chunk>

    @Before
    fun setup() {
        chunk = createObservableChunkMock(mockChunk())
        workbook = mockWorkbook(true)
        chapter = mockChapter()
        player = mockIAudioPlayer()
        recorder = mockIAudioRecorder()
        audioFileUtils = mockAudioFileUtils()
        splitAudioOnCues = mockSplitAudioOnCues()
    }

    fun mockWorkbook(withAudio: Boolean) : Workbook {
        val audioDirectory = if (withAudio) testDirWithAudio else testDirWithoutAudio
        return mockk<Workbook>{
            every { projectFilesAccessor.getChapterAudioDir(any(), any())} returns File(audioDirectory)
        }
    }

    val mockChunkCount = Single.just(31)
    fun mockChapter() : Chapter {
        return mockk<Chapter> {
            every { chunkCount } returns mockChunkCount
            every { getDraft() } returns chunk
            every { hasSelectedAudio() } returns false
            every { getSelectedTake() } returns null
        }
    }

    val verseChunkNum = 0
    fun mockChunk() : Chunk {
        return mockk<Chunk>{
            every { start } returns verseChunkNum + 1
            every { end } returns verseChunkNum + 1
        }
    }

    private fun createObservableChunkMock(chunk: Chunk): Observable<Chunk> {
        return Observable.just(chunk, mockChunk(), mockChunk()) // TODO: note that this is what is allowing me to show 3 verse markers.
    }

    fun mockIAudioPlayer() : IAudioPlayer {

        return mockk<IAudioPlayer>{
            val audioFileReader = mockk<AudioFileReader>()
            every { load(match<AudioFileReader> { true }) } returns Unit
            every { isPlaying() } returns false
            every { pause() } returns Unit
            every { getLocationInFrames() } returns 0
            every { getDurationInFrames() } returns 0
            every { seek(match<Int> { true }) } returns Unit
        }
    }

    fun mockIAudioRecorder() : IAudioRecorder {
        val byteArrayObservable: Observable<ByteArray> = Observable.empty()

        return mockk<IAudioRecorder>{
            every {getAudioStream()} returns byteArrayObservable
            every { start() } returns Unit
        }
    }

    fun mockAudioFileUtils() : AudioFileUtils {
        return mockk<AudioFileUtils>{}
    }

    fun mockSplitAudioOnCues() : SplitAudioOnCues {
        return mockk<SplitAudioOnCues>{}
    }

    @Test
    fun `activeVerses test with empty verses list`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // TODO: get active verses by adding a JSON file with the expected data
        val verses = narration.activeVerses

        println("test")
    }

}