package org.wycliffeassociates.otter.common.domain.narration

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
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

class Narration @AssistedInject constructor(
    private val splitAudioOnCues: SplitAudioOnCues,
    private val audioFileUtils: AudioFileUtils,
    private val recorder: IAudioRecorder,
    private val player: IAudioPlayer,
    @Assisted private val workbook: Workbook,
    @Assisted private val chapter: Chapter
) {
    private val logger = LoggerFactory.getLogger(Narration::class.java)

    private val history = NarrationHistory()
    private var chapterRepresentation = ChapterRepresentation(workbook, chapter)

    val workingAudio: AudioFile
        get() = chapterRepresentation.scratchAudio

    val audioReader: AudioFileReader
        get() = chapterRepresentation

    val activeVerses: List<VerseMarker>
        get() = run {
            val verses = chapterRepresentation
                .activeVerses
                .map {
                    it.marker
                }
            verses
        }

    val onActiveVersesUpdated: PublishSubject<List<VerseMarker>>
        get() = chapterRepresentation.onActiveVersesUpdated

    private val firstVerse: VerseMarker

    private var writer: WavFileWriter? = null

    init {
        initializeWavWriter()

        firstVerse = getFirstVerseMarker()
        updateWorkingFilesFromChapterFile()
        chapterRepresentation.loadFromSerializedVerses()
    }

    private fun getFirstVerseMarker(): VerseMarker {
        val firstVerse = chapter.getDraft().blockingFirst()
        return VerseMarker(firstVerse.start, firstVerse.end, 0)
    }

    fun getPcmBuffer(bytes: ByteArray): Int {
        return chapterRepresentation.getPcmBuffer(bytes)
    }

    fun loadFromSelectedChapterFile() {
        updateWorkingFilesFromChapterFile(true)
    }

    fun getPlayer(): IAudioPlayer {
        return player
    }

    fun getRecorderAudioStream(): Observable<ByteArray> {
        return recorder.getAudioStream()
    }

    fun closeRecorder() {
        recorder.stop()

        writer?.writer?.dispose()
        writer = null
    }

    fun closeChapterRepresentation() {
        chapterRepresentation.close()
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
        val end = chapterRepresentation.finalizeVerse(verseIndex)
        history.finalizeVerse(end)
    }

    fun onNewVerse(verseIndex: Int) {
        val action = NewVerseAction(verseIndex)
        execute(action)

        recorder.start()
        writer?.start()
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verseIndex)
        execute(action)

        recorder.start()
        writer?.start()
    }

    fun onVerseMarker(verseIndex: Int, markerPosition: Int) {
        val action = VerseMarkerAction(verseIndex, markerPosition)
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
        recorder.pause()
        writer?.pause()
    }

    fun resumeRecording() {
        recorder.start()
        writer?.start()
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
        logger.info("Loading verse ${verse.label} into player")
        val range: IntRange? = chapterRepresentation.getRangeOfMarker(verse)
        logger.info("Playback range is ${range?.start}-${range?.last}")
        range?.let {
            player.loadSection(chapterRepresentation.scratchAudio.file, range.first, range.last)
        }
    }

    private fun initializeWavWriter() {
        writer = WavFileWriter(
            chapterRepresentation.scratchAudio,
            recorder.getAudioStream(),
            true
        ) {
            /* no op */
        }
    }

    private fun execute(action: NarrationAction) {
        history.execute(action, chapterRepresentation.totalVerses, chapterRepresentation.scratchAudio)
        chapterRepresentation.onVersesUpdated()
    }

    private fun updateWorkingFilesFromChapterFile(
        forceUpdate: Boolean = false
    ) {
        val chapterFile = chapter.getSelectedTake()?.file
        val chapterFileExists = chapterFile?.exists() ?: false

        val narrationEmpty = chapterRepresentation.scratchAudio.totalFrames == 0
        val narrationFromChapter = chapterFileExists && narrationEmpty

        if (narrationFromChapter || forceUpdate) {
            val segments = splitAudioOnCues.execute(chapterFile!!, firstVerse)
            createVersesFromVerseSegments(segments)
            appendVerseSegmentsToWorkingAudio(segments)
        }
    }

    private fun appendVerseSegmentsToWorkingAudio(segments: VerseSegments) {
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
}

@AssistedFactory
interface NarrationFactory {
    fun create(
        workbook: Workbook,
        chapter: Chapter
    ): Narration
}
