package org.wycliffeassociates.otter.common.domain.narration

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Narration @AssistedInject constructor(
    private val splitAudioOnCues: SplitAudioOnCues,
    private val audioFileUtils: AudioFileUtils,
    private val recorder: IAudioRecorder,
    private val player: IAudioPlayer,
    @Assisted private val workbook: Workbook,
    @Assisted private val chapter: Chapter
) {
    private val DEFAULT_FRAME_SIZE_BYTES = 2
    private val logger = LoggerFactory.getLogger(Narration::class.java)

    private val history = NarrationHistory()
    private var chapterRepresentation = ChapterRepresentation(workbook, chapter)
    private val chapterReaderConnection =
        chapterRepresentation.getAudioFileReader() as ChapterRepresentation.ChapterRepresentationConnection

    private var audioLoaded = false

    private val isRecording = AtomicBoolean(false)
    private val uncommittedRecordedFrames = AtomicInteger(0)

    private val disposables = CompositeDisposable()

    val audioReader: AudioFileReader
        get() = chapterRepresentation.getAudioFileReader()

    val activeVerses: List<VerseMarker>
        get() = run {
            val verses = chapterRepresentation
                .activeVerses
                .map {
                    it.marker.copy(
                        location = chapterRepresentation.absoluteToRelative(it.firstFrame())
                    )
                }
            verses
        }

    val onActiveVersesUpdated: PublishSubject<List<VerseMarker>>
        get() = chapterRepresentation.onActiveVersesUpdated

    private val firstVerse: VerseMarker

    private var writer: WavFileWriter? = null

    init {
        val writer = initializeWavWriter()

        firstVerse = getFirstVerseMarker()
        restoreFromExistingChapterAudio()
        chapterRepresentation.loadFromSerializedVerses()
        recorder.start()
        disposables.addAll(
            activeRecordingFrameCounter(writer),
            resetUncommittedFramesOnUpdatedVerses(),
        )
        loadChapterIntoPlayer()
    }

    /**
     * Counts the number of audio frames that have been recorded since activating a recording
     */
    private fun activeRecordingFrameCounter(writer: WavFileWriter): Disposable {
        return Observable
            .combineLatest(writer.isWriting, getRecorderAudioStream())
            { isWriting, bytes -> Pair(isWriting, bytes) }
            .map { (isWriting, bytes) ->
                if (isWriting) {
                    uncommittedRecordedFrames.addAndGet(bytes.size / DEFAULT_FRAME_SIZE_BYTES)
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    /**
     * Resets the count of recorded frames when the writer pauses and thus the active verse list gets updated
     */
    private fun resetUncommittedFramesOnUpdatedVerses(): Disposable {
        return onActiveVersesUpdated.subscribe {
            uncommittedRecordedFrames.set(0)
        }
    }

    private fun getFirstVerseMarker(): VerseMarker {
        val firstVerse = chapter.getDraft().blockingFirst()
        return VerseMarker(firstVerse.start, firstVerse.end, 0)
    }

    fun loadFromSelectedChapterFile() {
        restoreFromExistingChapterAudio(true)
    }

    fun getPlayer(): IAudioPlayer {
        return player
    }

    fun getRecorderAudioStream(): Observable<ByteArray> {
        return recorder.getAudioStream()
    }

    fun closeRecorder() {
        isRecording.set(false)
        recorder.stop()

        writer?.writer?.dispose()
        writer = null
    }

    fun closeChapterRepresentation() {
        chapterRepresentation.closeConnections()
    }

    fun undo() {
        history.undo(chapterRepresentation.totalVerses)
        chapterRepresentation.onVersesUpdated()
    }

    fun redo() {
        history.redo(chapterRepresentation.totalVerses)
        chapterRepresentation.onVersesUpdated()
    }

    fun finalizeVerse(verseIndex: Int) {
        val loc = chapterRepresentation.finalizeVerse(verseIndex, history)
        seek(loc)
    }

    fun onNewVerse(verseIndex: Int) {
        loadChapterIntoPlayer()
        val action = NewVerseAction(verseIndex)
        execute(action)

        player.seek(player.getDurationInFrames())
        writer?.start()
        isRecording.set(true)
    }

    fun onRecordAgain(verseIndex: Int) {
        loadChapterIntoPlayer()
        val action = RecordAgainAction(verseIndex)
        execute(action)

        player.seek(activeVerses[verseIndex].location)
        writer?.start()
        isRecording.set(true)
    }

    fun onVerseMarkerMoved(verseIndex: Int, delta: Int) {
        val action = MoveMarkerAction(verseIndex, delta)
        execute(action)
    }

    fun onEditVerse(verseIndex: Int, editedFile: File) {
        val start = chapterRepresentation.scratchAudio.totalFrames
        audioFileUtils.appendFile(chapterRepresentation.scratchAudio, editedFile)
        val end = chapterRepresentation.scratchAudio.totalFrames

        val action = EditVerseAction(verseIndex, start, end)
        execute(action)
    }

    fun onResetAll() {
        val action = ResetAllAction()
        execute(action)
    }

    private fun onChapterEdited(newVerses: List<VerseNode>) {
        // TODO adjust for not having an end location
        val action = ChapterEditedAction(newVerses)
        execute(action)
    }

    fun pauseRecording() {
        writer?.pause()
        isRecording.set(false)
    }

    fun resumeRecording() {
        player.seek(player.getDurationInFrames())
        writer?.start()
        isRecording.set(true)
    }

    fun hasUndo(): Boolean {
        return history.hasUndo()
    }

    fun hasRedo(): Boolean {
        return history.hasRedo()
    }

    fun getSectionAsFile(index: Int): File {
        val verse = activeVerses[index]
        return audioFileUtils.getSectionAsFile(
            chapterRepresentation.scratchAudio,
            verse.start,
            verse.end
        )
    }

    fun loadSectionIntoPlayer(verse: VerseMarker) {
        if (!audioLoaded) {
            player.load(chapterReaderConnection)
            audioLoaded = true
        }
        logger.info("Loading verse ${verse.label} into player")
        val range: IntRange? = chapterRepresentation.getRangeOfMarker(verse)
        logger.info("Playback range is ${range?.start}-${range?.last}")
        range?.let {
            val wasPlaying = player.isPlaying()
            player.pause()
            chapterReaderConnection.lockToVerse(activeVerses.indexOf(verse))
            chapterReaderConnection.start = range.first
            chapterReaderConnection.end = range.last
            player.seek(range.first)
            chapterReaderConnection.seek(range.first)
            if (wasPlaying) player.play()
        }
    }

    fun loadChapterIntoPlayer() {
        if (!audioLoaded) {
            player.load(chapterReaderConnection)
            audioLoaded = true
        }

        val wasPlaying = player.isPlaying()
        player.pause()

        chapterReaderConnection.lockToVerse(null)
        chapterReaderConnection.start = null
        chapterReaderConnection.end = null

        if (player.getLocationInFrames() == player.getDurationInFrames()) {
            player.seek(0)
            chapterReaderConnection.seek(0)
        }

        if (wasPlaying) player.play()
    }

    private fun initializeWavWriter(): WavFileWriter {
        writer = WavFileWriter(
            chapterRepresentation.scratchAudio,
            recorder.getAudioStream(),
            true
        ) {
            /* no op */
        }
        return writer!!
    }

    private fun execute(action: NarrationAction) {
        chapterReaderConnection.lockToVerse(null)
        history.execute(action, chapterRepresentation.totalVerses, chapterRepresentation.scratchAudio)
        chapterRepresentation.onVersesUpdated()
    }

    private fun restoreFromExistingChapterAudio(
        forceUpdate: Boolean = false
    ) {
        val chapterFile = chapter.getSelectedTake()?.file
        val chapterFileExists = chapterFile?.exists() ?: false

        val narrationEmpty = chapterRepresentation.scratchAudio.totalFrames == 0
        val narrationFromChapter = chapterFileExists && narrationEmpty

        if (narrationFromChapter || forceUpdate) {
            val segments = splitAudioOnCues.execute(chapterFile!!, firstVerse)
            createVersesFromVerseSegments(segments)
            appendVerseSegmentsToScratchAudio(segments)
        }
    }

    private fun appendVerseSegmentsToScratchAudio(segments: VerseSegments) {
        segments.forEach {
            audioFileUtils.appendFile(chapterRepresentation.scratchAudio, it.value)
        }
    }

    private fun createVersesFromVerseSegments(segments: VerseSegments) {
        val nodes = mutableListOf<VerseNode>()
        var start = chapterRepresentation.scratchAudio.totalFrames
        var end = chapterRepresentation.scratchAudio.totalFrames

        segments.forEach {
            val verseAudio = AudioFile(it.value)
            end += verseAudio.totalFrames
            val node = VerseNode(
                start,
                end,
                true,
                it.key
            )
            nodes.add(node)
            start = end
        }

        onChapterEdited(nodes)
    }

    fun scrollAudio(delta: Int) {
        chapterReaderConnection.seek(delta)
    }

    fun seek(location: Int) {
        player.seek(location)
        chapterReaderConnection.seek(location)
    }

    fun getLocationInFrames(): Int {
        return player.getLocationInFrames() + uncommittedRecordedFrames.get()
    }

    fun getTotalFrames(): Int {
        return chapterReaderConnection.totalFrames + uncommittedRecordedFrames.get()
    }

    fun close() {
        disposables.dispose()
        player.stop()
        player.close()
        recorder.stop()
        chapterRepresentation.closeConnections()
    }

    fun seekToPrevious() {
        player.pause()
        val loc = player.getLocationInFrames()
        val seekLoc = activeVerses.lastOrNull() { it.location < loc }
        seekLoc?.let {
            seek(it.location)
        } ?: seek(0)
    }

    fun seekToNext() {
        player.pause()
        val loc = player.getLocationInFrames()
        val seekLoc = activeVerses.firstOrNull { it.location > loc }
        seekLoc?.let {
            seek(it.location)
        } ?: seek(player.getDurationInFrames())
    }
}

@AssistedFactory
interface NarrationFactory {
    fun create(
        workbook: Workbook,
        chapter: Chapter
    ): Narration
}
