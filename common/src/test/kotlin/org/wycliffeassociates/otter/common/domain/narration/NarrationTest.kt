package org.wycliffeassociates.otter.common.domain.narration
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.absoluteValue


class NarrationTest {
    val testDataRootFilePath: String = System.getProperty("user.home")
    val testDirWithAudio = File(testDataRootFilePath, "testProjectChapterDirWithAudio")
    val testDirWithoutAudio = File(testDataRootFilePath, "testProjectChapterDirWithoutAudio")
    val verseEditFile = File(testDirWithAudio, "verseEditFile.pcm")

    lateinit var workbook: Workbook
    lateinit var chapter: Chapter
    lateinit var player: IAudioPlayer
    lateinit var recorder: IAudioRecorder
    lateinit var audioFileUtils: AudioFileUtils
    lateinit var splitAudioOnCues: SplitAudioOnCues
    lateinit var chunk: Observable<Chunk>
    val numTestVerses = 31

    @Before
    fun setup() {
        createTestAudioFolders()
        chunk = createObservableChunkMock()
        workbook = mockWorkbook(true)
        chapter = mockChapter()
        player = mockIAudioPlayer()
        recorder = mockIAudioRecorder()
        audioFileUtils = AudioFileUtils(mockIDirectoryProvider())
        splitAudioOnCues = mockSplitAudioOnCues()
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
        val testProjectChapterDirWithoutAudio = "testProjectChapterDirWithoutAudio" // Replace with the desired folder name

        val withAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithAudio)
        val withoutAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithoutAudio)

        try {
            Files.createDirectories(withAudioPath)
            Files.createDirectories(withoutAudioPath)
        } catch (e: Exception) {
            println("Failed to create test audio folders' at '$testDataRootFilePath': ${e.message}")
        }
    }

    fun mockWorkbook(withAudio: Boolean) : Workbook {
        val audioDirectory = if (withAudio) testDirWithAudio else testDirWithoutAudio
        return mockk<Workbook>{
            every { projectFilesAccessor.getChapterAudioDir(any(), any())} returns audioDirectory
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

    var verseChunkNum = 0
    fun mockChunk() : Chunk {
        verseChunkNum += 1
        return mockk<Chunk>{
            every { start } returns verseChunkNum
            every { end } returns verseChunkNum
        }
    }

    private fun createObservableChunkMock(): Observable<Chunk> {
        val numberOfChunks = numTestVerses
        val chunkList = List(numberOfChunks) { mockChunk() }
        return Observable.concat(chunkList.map { Observable.just(it) })
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

    private val byteArraySubject = PublishSubject.create<ByteArray>()
    fun mockIAudioRecorder() : IAudioRecorder {
        return mockk<IAudioRecorder>{
            every {getAudioStream()} returns byteArraySubject
            every { start() } returns Unit
        }
    }

    // Function to add bytes to the observable
    fun addBytesToRecorderAudioStream(byteArray: ByteArray) {
        byteArraySubject.onNext(byteArray)
    }

    fun mockIDirectoryProvider() : IDirectoryProvider {
        return mockk<IDirectoryProvider>(){
            every { createTempFile(any(), any()) } returns verseEditFile
        }
    }


    fun mockSplitAudioOnCues() : SplitAudioOnCues {
        return mockk<SplitAudioOnCues>{}
    }


    private fun writeByteBufferToPCMFile(byteBuffer: ByteBuffer, PCMFile: File) {
        try {
            val byteArray = ByteArray(byteBuffer.remaining())
            byteBuffer.get(byteArray)

            val fos = FileOutputStream(PCMFile, false)

            fos.write(byteArray)

            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        byteBuffer.rewind()
    }


    fun fillAudioBufferWithPadding(byteBuffer: ByteBuffer, secondsOfAudio: Int, paddingLength: Int) {
        for (i in 1 .. secondsOfAudio) {
            for(j in 1 .. DEFAULT_SAMPLE_RATE) {
                byteBuffer.putShort(i.toShort())
            }
            for(j in 1 .. paddingLength) {
                byteBuffer.putShort(0)
            }
        }
        byteBuffer.rewind()
    }

    fun addNewVerseWithAudio(narration: Narration, verseIndex: Int, secondsOfAudio: Int) {
        narration.onNewVerse(verseIndex)
        // Defaults to adding the verseIndex as a byte, so I can have something to test
        val bytesToAdd = ByteArray(DEFAULT_SAMPLE_RATE * 2 * secondsOfAudio) {verseIndex.toByte()}
        addBytesToRecorderAudioStream(bytesToAdd)
        narration.pauseRecording()
    }


    fun recordAndFinalizeVerses(narration: Narration, numVerses: Int, verseRecordingLength: Int) {
        for(i in 0 until numVerses) {
            addNewVerseWithAudio(narration, i, verseRecordingLength)
            narration.finalizeVerse(i)
        }
    }

    fun makeVerseEditFile(verseEditFile: File, verseRecordingLengthInSeconds: Int) {
        val verseEditFileByteBuffer = ByteBuffer.allocate(DEFAULT_SAMPLE_RATE * verseRecordingLengthInSeconds * 2)

        fillAudioBufferWithPadding(verseEditFileByteBuffer, verseRecordingLengthInSeconds, 0)
        writeByteBufferToPCMFile(verseEditFileByteBuffer, verseEditFile)
    }


    fun checkBytesInFile(file: File, targetByte: Byte): Boolean {

        if (!file.exists() || !file.isFile) {
            throw IllegalArgumentException("The specified file does not exist or is not a regular file.")
        }

        var inputStream: FileInputStream? = null
        var bytesRead = 0
        try {
            inputStream = FileInputStream(file)
            var byteRead: Int
            while (inputStream.read().also { byteRead = it } != -1) {
                if (byteRead.toByte() != targetByte) {
                    return false
                }
                bytesRead++
            }

            return true
        } catch (e: IOException) {
            // Handle any potential IO exceptions here
            return false
        } finally {
            inputStream?.close()
        }
    }


    @Test
    fun `activeVerses test with no placed verses`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        Assert.assertEquals(0, narration.activeVerses.size)
    }


    @Test
    fun `onNewVerse with index greater than totalVerses size`() {

        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        try {
            narration.onNewVerse(10000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (indexOutOfBounds : IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }

    @Test
    fun `onNewVerse with negative index`() {

        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        try {
            narration.onNewVerse(-1000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (indexOutOfBounds : IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }


    @Test
    fun `onNewVerse with valid index`() {
        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        Assert.assertEquals(false, narration.hasUndo())
        Assert.assertEquals(0, narration.activeVerses.size)
        narration.onNewVerse(verseIndex)
        Assert.assertEquals(1, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-${verseIndex + 1}", narration.activeVerses[verseIndex].formattedLabel)
        Assert.assertEquals(true, narration.hasUndo())
    }


    @Test
    fun `finalize with verseIndex equal to 0 and 10 seconds of scratch audio`() {
        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val secondsOfRecordedAudio = 10

        // Verify that no audio has been recorded and no verses have been placed
        Assert.assertEquals(0, narration.activeVerses.size)

        // writes 10 seconds of audio
        addNewVerseWithAudio(narration, verseIndex,secondsOfRecordedAudio)

        narration.finalizeVerse(verseIndex)

        // Verify that audio was recorded
        Assert.assertEquals(DEFAULT_SAMPLE_RATE * secondsOfRecordedAudio, narration.audioReader.totalFrames)

        // Verify that activeVerses has been updated and that it contains the first verse
        Assert.assertEquals(1, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-${verseIndex + 1}", narration.activeVerses[verseIndex].formattedLabel)
    }


    // NOTE: this will let me record again, even though I have not placed a verse
    @Test
    fun `onRecordAgain with verseIndex equal to 0 and empty activeVerses`() {
        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        narration.onRecordAgain(verseIndex)
        narration.pauseRecording()
        narration.finalizeVerse(verseIndex)
        Assert.assertEquals(0, narration.audioReader.totalFrames)
    }

    @Test
    fun `onRecordAgain with five seconds of new audio, 10 seconds of existing audio, verseIndex equal to 0, and non-empty activeVerses`() {
        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val secondsOfAudioBeforeRecordAgain = 10
        val secondsOfAudioAfterRecordAgain = 5
        // writes 10 seconds of audio
        addNewVerseWithAudio(narration, verseIndex, secondsOfAudioBeforeRecordAgain)
        // Finalizes the added verse
        narration.finalizeVerse(verseIndex)

        Assert.assertEquals(DEFAULT_SAMPLE_RATE * secondsOfAudioBeforeRecordAgain, narration.audioReader.totalFrames)

        narration.onRecordAgain(verseIndex)
        addBytesToRecorderAudioStream(ByteArray(DEFAULT_SAMPLE_RATE * 2 * secondsOfAudioAfterRecordAgain) {1})
        narration.pauseRecording()
        narration.finalizeVerse(verseIndex)

        // Verify that audio was recorded
        val expectedFramesAfterRecordAgain = DEFAULT_SAMPLE_RATE * secondsOfAudioAfterRecordAgain
        Assert.assertEquals(expectedFramesAfterRecordAgain, narration.audioReader.totalFrames)
        // Verify that activeVerses has been updated and that it contains only the first verse
        Assert.assertEquals(1, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-${verseIndex + 1}", narration.activeVerses[verseIndex].formattedLabel)
    }


    @Test
    fun `getTotalFrames with no committed or uncommitted frames`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        Assert.assertEquals(0, narration.getTotalFrames())
    }


    @Test
    fun `getTotalFrames with uncommitted frames and no committed frames`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val verseIndex = 0
        val secondsOfAudio = 1
        addNewVerseWithAudio(narration, verseIndex, secondsOfAudio)

        val totalFrames = narration.getTotalFrames()

        // NOTE: the + 1 comes from how NewVerse is implemented. NewVerse calls addStart, which adds a sector with
        // a range of start <= .. <= start. This results in the additional frame being added, because that sector is not
        // updated with the actual end value until after the frames are committed.
        Assert.assertEquals(DEFAULT_SAMPLE_RATE * secondsOfAudio + 1, totalFrames)
    }

    @Test
    fun `getTotalFrames with committed frames and no uncommitted frames`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val numVersesToRecord = 1
        val secondsRecordedPerVerse = 1
        recordAndFinalizeVerses(narration, numVersesToRecord, secondsRecordedPerVerse)

        val totalFrames = narration.getTotalFrames()

        Assert.assertEquals(DEFAULT_SAMPLE_RATE * secondsRecordedPerVerse * numVersesToRecord, totalFrames)
    }


    @Test
    fun `onVerseMarkerMoved with verseIndex greater than activeVerses size`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        try {
            narration.onVerseMarkerMoved(1000, 5000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (indexOutOfBounds: IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }

    @Test
    fun `onVerseMarkerMoved negative verseIndex`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        try {
            narration.onVerseMarkerMoved(-1000, 5000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (indexOutOfBounds: IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }


    @Test
    fun `onVerseMarkerMoved with verseIndex equal to 2, delta equal to 44100, and existing workingAudio`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val numVersesToRecord = 2
        val secondsPerVerse = 10

        // Simulates recording two verses that are 10 seconds long
        recordAndFinalizeVerses(narration, numVersesToRecord, secondsPerVerse)

        // Verify that we have two verses with 10 seconds each
        val expectedTotalFrames = DEFAULT_SAMPLE_RATE * secondsPerVerse * numVersesToRecord
        Assert.assertEquals(expectedTotalFrames, narration.getTotalFrames())
        Assert.assertEquals(numVersesToRecord, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-1", narration.activeVerses[0].formattedLabel)
        Assert.assertEquals("orature-vm-2", narration.activeVerses[1].formattedLabel)

        // Verify that verse 2's location is correct
        val oldVerseLocation = DEFAULT_SAMPLE_RATE * secondsPerVerse * (numVersesToRecord - 1)
        Assert.assertEquals(oldVerseLocation, narration.activeVerses[1].location)

        val delta = 44100
        narration.onVerseMarkerMoved(1, delta)

        // Verify that we have the same number of frames as before
        Assert.assertEquals(expectedTotalFrames, narration.getTotalFrames())
        // Verify that the new location of verse two is correct.
        Assert.assertEquals(oldVerseLocation + delta, narration.activeVerses[1].location)
    }

    @Test
    fun `onVerseMarkerMoved with verseIndex equal to 2, delta equal to -44100, and existing workingAudio`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val numVersesToRecord = 2
        val secondsPerVerse = 10

        // Simulates recording two verses that are 10 seconds long
        recordAndFinalizeVerses(narration, numVersesToRecord, secondsPerVerse)

        // Verify that we have two verses with 10 seconds each
        val expectedTotalFrames = DEFAULT_SAMPLE_RATE * secondsPerVerse * numVersesToRecord
        Assert.assertEquals(expectedTotalFrames, narration.getTotalFrames())
        Assert.assertEquals(numVersesToRecord, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-1", narration.activeVerses[0].formattedLabel)
        Assert.assertEquals("orature-vm-2", narration.activeVerses[1].formattedLabel)

        // Verify that verse 2's location is correct
        val oldVerseLocation = DEFAULT_SAMPLE_RATE * secondsPerVerse * (numVersesToRecord - 1)
        Assert.assertEquals(oldVerseLocation, narration.activeVerses[1].location)

        val delta = -44100
        narration.onVerseMarkerMoved(1, delta)

        // Verify that we have the same number of frames as before
        Assert.assertEquals(expectedTotalFrames, narration.getTotalFrames())
        // Verify that the new location of verse two is correct.
        Assert.assertEquals(oldVerseLocation + delta, narration.activeVerses[1].location)
    }


    // NOTE: this may indicate odd behavior because it allows us to place a verseMarker with no audio
    // corresponding to it.
    @Test
    fun `onEditVerse with no scratch audio, no placed verses, and empty edited audio file`() {
        makeVerseEditFile(verseEditFile, 0)

        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // Verify that no verseMarkers have been placed
        Assert.assertEquals(0, narration.activeVerses.size)

        // Verify that there is no scratch audio
        Assert.assertEquals(0, narration.audioReader.totalFrames)

        narration.onEditVerse(verseIndex, verseEditFile)

        // Verify that verseMarker 1 have been placed
        Assert.assertEquals(1, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-${verseIndex + 1}", narration.activeVerses[verseIndex].formattedLabel)

        Assert.assertEquals(0, narration.audioReader.totalFrames)
    }

    @Test
    fun `onEditVerse with no scratch audio, no placed verses, and edited audio with 10 seconds`() {
        val editedVerseIndex = 0
        val verseEditSeconds = 10
        makeVerseEditFile(verseEditFile, verseEditSeconds)

        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // Verify that no verseMarkers have been placed
        Assert.assertEquals(0, narration.activeVerses.size)

        // Verify that there is no scratch audio
        Assert.assertEquals(0, narration.audioReader.totalFrames)

        narration.onEditVerse(editedVerseIndex, verseEditFile)

        // Verify that verseMarker 1 have been placed
        Assert.assertEquals(1, narration.activeVerses.size)
        val expectedFormattedLabel = "orature-vm-${editedVerseIndex + 1}"
        Assert.assertEquals(expectedFormattedLabel, narration.activeVerses[editedVerseIndex].formattedLabel)

        // Verify that there is 10 seconds of scratch audio
        Assert.assertEquals(DEFAULT_SAMPLE_RATE * verseEditSeconds, narration.audioReader.totalFrames)
    }


    @Test
    fun `onEditVerse with existing scratch audio, 10 placed verses, editing verse 5`() {
        makeVerseEditFile(verseEditFile, 10)

        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        val numberOfRecordedVerses = 10
        val secondsPerVerse = 1
        recordAndFinalizeVerses(narration, numberOfRecordedVerses, secondsPerVerse)

        // Verify that 10 verseMarkers have been placed
        Assert.assertEquals(numberOfRecordedVerses, narration.activeVerses.size)

        // Verify that there is 10 seconds of scratch audio
        val numberAudioFramesBeforeEdit = DEFAULT_SAMPLE_RATE*numberOfRecordedVerses*secondsPerVerse
        Assert.assertEquals(numberAudioFramesBeforeEdit, narration.audioReader.totalFrames)

        val editedVerseIndex = 4
        narration.onEditVerse(editedVerseIndex, verseEditFile)

        // Verify that there is 9 seconds from un-edited verses, and 10 from edited verse
        val expectedTotalFrames = DEFAULT_SAMPLE_RATE * 10 + DEFAULT_SAMPLE_RATE * 9
        Assert.assertEquals(expectedTotalFrames, narration.audioReader.totalFrames)
    }

    @Test
    fun `onResetAll with non-empty activeVerses`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val numberOfRecordedVerses = 10
        val secondsPerVerse = 1
        recordAndFinalizeVerses(narration, numberOfRecordedVerses, secondsPerVerse)

        // Verify that 10 verseMarkers have been placed
        Assert.assertEquals(numberOfRecordedVerses, narration.activeVerses.size)

        // Verify that there is 10 seconds of scratch audio
        val numberAudioFramesBeforeEdit = DEFAULT_SAMPLE_RATE * numberOfRecordedVerses * secondsPerVerse
        Assert.assertEquals(numberAudioFramesBeforeEdit, narration.audioReader.totalFrames)

        narration.onResetAll()

        // Verify that no verseMarkers have been placed
        Assert.assertEquals(0, narration.activeVerses.size)
        // Verify that total frames is 0
        Assert.assertEquals(0, narration.getTotalFrames())
    }


    @Test
    fun `getSectionAsFile with index not in activeVerses`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        try {
            narration.getSectionAsFile(1000)
            Assert.fail("Expecting IndexOutOfBoundsException")
        } catch (indexOutOfBounds: IndexOutOfBoundsException) {
            // Success: expecting exception
        }
    }


    @Test
    fun `getSectionAsFile non-empty activeVerses and valid index`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val secondsPerVerse = 1
        val numberOfRecordedVerses = 1
        recordAndFinalizeVerses(narration, numberOfRecordedVerses, secondsPerVerse)


        val verseOneFile = narration.getSectionAsFile(0)

        // Verify that the file has bytes equal to the verseIndex
        val hasCorrectBytes = checkBytesInFile(verseOneFile, 0)

        Assert.assertTrue(hasCorrectBytes)
        // TODO: figure out why this is failing. Seems to always be 88214.
        // Verify that the file representing the verse is of the correct size
        val expectedByteSize = DEFAULT_SAMPLE_RATE * numberOfRecordedVerses*secondsPerVerse * DEFAULT_BITS_PER_SAMPLE / 8
        Assert.assertEquals(expectedByteSize, verseOneFile.length())
    }


    @Test
    fun `onNewVerse, pauseRecording, finalizeVerse, onRecordAgain, undo, then redo`() {
        val verseIndex = 0
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)
        val secondsBeforeRecordAgain = 1
        val secondsAfterRecordAgain = 5

        // Verify that no verses have been placed
        Assert.assertEquals(0, narration.activeVerses.size)

        // Add new verse
        narration.onNewVerse(verseIndex)

        // verify that only the expected verse has been placed
        Assert.assertEquals(1, narration.activeVerses.size)
        Assert.assertEquals("orature-vm-${verseIndex + 1}", narration.activeVerses[verseIndex].formattedLabel)

        // Pause and finalize the recording for specified verse
        var numberOfBytesStreamed = DEFAULT_SAMPLE_RATE * DEFAULT_BITS_PER_SAMPLE / 8 * secondsBeforeRecordAgain
        val originalRecordingSizeInFrames = DEFAULT_SAMPLE_RATE * secondsBeforeRecordAgain

        addBytesToRecorderAudioStream(ByteArray(numberOfBytesStreamed) {1})
        narration.pauseRecording()
        narration.finalizeVerse(verseIndex)

        // Verify that the audio for specified verse is one seconds long
        Assert.assertEquals(originalRecordingSizeInFrames, narration.audioReader.totalFrames)

        // Record again
        numberOfBytesStreamed = DEFAULT_SAMPLE_RATE * DEFAULT_BITS_PER_SAMPLE / 8 * secondsAfterRecordAgain
        val newRecordingSizeInFrames = DEFAULT_SAMPLE_RATE * secondsAfterRecordAgain

        narration.onRecordAgain(verseIndex)
        addBytesToRecorderAudioStream(ByteArray(numberOfBytesStreamed) {1})

        // Pause and finalize recording
        narration.pauseRecording()
        narration.finalizeVerse(verseIndex)

        // Verify that the specified verse is of the correct size
        Assert.assertEquals(newRecordingSizeInFrames, narration.audioReader.totalFrames)

        // undo
        narration.undo()
        Assert.assertEquals(originalRecordingSizeInFrames, narration.audioReader.totalFrames)

        // redo
        narration.redo()
        Assert.assertEquals(newRecordingSizeInFrames, narration.audioReader.totalFrames)

    }

    @Test
    fun `record and finalize 10 verses, onVerseMarkerMoved, undo, then redo`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // record and finalize 10 verses
        recordAndFinalizeVerses(narration, 10, 1)

        val verseIndexToMove = 4
        val delta = 1000
        val oldVerseLocation = narration.activeVerses[verseIndexToMove].location

        // Move verse
        narration.onVerseMarkerMoved(verseIndexToMove, 1000)

        // Verify that the verse has been moved to the correct location
        Assert.assertEquals(oldVerseLocation + delta, narration.activeVerses[verseIndexToMove].location)

        narration.undo()

        // Verify that the verse has been restored to its original location
        Assert.assertEquals(oldVerseLocation, narration.activeVerses[verseIndexToMove].location)

        narration.redo()

        // Verify that the verse has been moved to the correct location
        Assert.assertEquals(oldVerseLocation + delta, narration.activeVerses[verseIndexToMove].location)

    }


    @Test
    fun `record and finalize 10 verses, onEditVerse, undo, then redo`() {

        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // record and finalize 10 verses
        recordAndFinalizeVerses(narration, 10, 1)

        val verseIndexToEdit = 4
        val verseLengthInSecondsAfterEdit = 10
        val verseLengthInSecondsBeforeEdit = 1

        // Calculate the space between verse 5 and 6 before editing verse 5
        val spaceBetweenEditedAndNextVerseBeforeEdit = narration.activeVerses[verseIndexToEdit + 1].location -
                narration.activeVerses[verseIndexToEdit].location

        // Calculate the space between verses after editing
        val spaceBetweenEditedAndNextVerseAfterEdit = narration.activeVerses[verseIndexToEdit + 1].location -
                narration.activeVerses[verseIndexToEdit].location +
                (verseLengthInSecondsBeforeEdit - verseLengthInSecondsAfterEdit).absoluteValue * DEFAULT_SAMPLE_RATE

        // Edit verse
        makeVerseEditFile(verseEditFile, verseLengthInSecondsAfterEdit)
        narration.onEditVerse(verseIndexToEdit, verseEditFile)

        // Verify that the space between verses has changed correctly after edit
        Assert.assertEquals(spaceBetweenEditedAndNextVerseAfterEdit,
            narration.activeVerses[verseIndexToEdit + 1].location - narration.activeVerses[verseIndexToEdit].location)

        narration.undo()

        // Verify that the space between verses has changed correctly after undo
        Assert.assertEquals(spaceBetweenEditedAndNextVerseBeforeEdit,
            narration.activeVerses[verseIndexToEdit + 1].location - narration.activeVerses[verseIndexToEdit].location)

        narration.redo()

        // Verify that the space between verses has changed correctly after redo
        Assert.assertEquals(spaceBetweenEditedAndNextVerseAfterEdit,
            narration.activeVerses[verseIndexToEdit + 1].location - narration.activeVerses[verseIndexToEdit].location)
    }


    @Test
    fun `getLocationInFrames 1 second of uncommitted frames`() {
        val narration = Narration(splitAudioOnCues, audioFileUtils, recorder, player, workbook, chapter)

        // record and finalize 10 verses
        val verseIndex = 0
        val secondsOfAudio = 1
        addNewVerseWithAudio(narration, verseIndex, secondsOfAudio)

        Assert.assertEquals(DEFAULT_SAMPLE_RATE * secondsOfAudio, narration.getLocationInFrames())
    }

}