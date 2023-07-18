package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File

class Narration private constructor(
    private val splitAudioOnCues: SplitAudioOnCues,
    private val audioFileUtils: AudioFileUtils,
    private val recorder: IAudioRecorder,
    private val player: IAudioPlayer
) {
    private val history = NarrationHistory()
    private lateinit var chapterRepresentation: ChapterRepresentation

    val activeVerses: List<VerseNode>
        get() = chapterRepresentation.activeVerses

    val onActiveVersesUpdated: PublishSubject<List<VerseNode>>
        get() = chapterRepresentation.onActiveVersesUpdated

    private var writer: WavFileWriter? = null

    companion object {
        fun load(
            workbook: Workbook,
            chapter: Chapter,
            splitAudioOnCues: SplitAudioOnCues,
            audioFileUtils: AudioFileUtils,
            recorder: IAudioRecorder,
            player: IAudioPlayer,
            forceLoadFromChapterFile: Boolean = false
        ): Narration {
            return Narration(splitAudioOnCues, audioFileUtils, recorder, player).apply {
                chapterRepresentation = ChapterRepresentation(
                    workbook,
                    chapter
                )

                val chapterFile = chapter.getSelectedTake()?.file
                val chapterFileExists = chapterFile?.exists() ?: false

                val forceLoad = chapterFileExists && forceLoadFromChapterFile
                val narrationEmpty = chapterRepresentation.workingAudio.totalFrames == 0
                val narrationFromChapter = chapterFileExists && narrationEmpty

                if(forceLoad || narrationFromChapter) {
                    createWorkingFilesFromChapterFile(chapterFile!!)
                } else {
                    chapterRepresentation.loadFromSerializedVerses()
                }

                initializeWavWriter()
                chapterRepresentation.sendActiveVerses()
            }
        }
    }

    fun closeRecorder() {
        recorder.stop()

        writer?.writer?.dispose()
        writer = null
    }

    fun undo() {
        history.undo(chapterRepresentation.activeVerses)

        chapterRepresentation.sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    fun redo() {
        history.redo(chapterRepresentation.activeVerses)

        chapterRepresentation.sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    fun finalizeVerse(verseIndex: Int = activeVerses.lastIndex) {
        chapterRepresentation.finalizeVerse(verseIndex)
    }

    fun onNewVerse() {
        val action = NewVerseAction()
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

    fun onVerseMarker(firstVerseIndex: Int, secondVerseIndex: Int, markerPosition: Int) {
        val action = VerseMarkerAction(firstVerseIndex, secondVerseIndex, markerPosition)
        execute(action)
    }

    fun onEditVerse(verseIndex: Int, editedFile: File) {
        val start = chapterRepresentation.workingAudio.totalFrames
        audioFileUtils.appendFile(chapterRepresentation.workingAudio, editedFile)
        val end = chapterRepresentation.workingAudio.totalFrames

        val action = EditVerseAction(verseIndex, start, end)
        execute(action)
    }

    fun onResetAll() {
        val action = ResetAllAction()
        execute(action)
    }

    fun onChapterEdited(newVerses: List<VerseNode>) {
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
            chapterRepresentation.workingAudio,
            verse.start,
            verse.end
        )
    }

    fun loadSectionIntoPlayer(verse: VerseNode) {
        player.loadSection(chapterRepresentation.workingAudio.file, verse.start, verse.end)
    }

    private fun initializeWavWriter() {
        writer = WavFileWriter(
            chapterRepresentation.workingAudio,
            recorder.getAudioStream(),
            true
        ) {
            /* no op */
        }
    }

    private fun execute(action: NarrationAction) {
        history.execute(action, chapterRepresentation.activeVerses, chapterRepresentation.workingAudio)

        chapterRepresentation.sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    private fun createWorkingFilesFromChapterFile(file: File) {
        val segments = splitAudioOnCues.execute(file)
        createVersesFromVerseSegments(segments)
        appendVerseSegmentsToWorkingAudio(segments)
    }

    private fun appendVerseSegmentsToWorkingAudio(segments: Map<String, File>) {
        segments.forEach {
            audioFileUtils.appendFile(chapterRepresentation.workingAudio, it.value)
        }
    }

    private fun createVersesFromVerseSegments(segments: Map<String, File>) {
        val nodes = mutableListOf<VerseNode>()
        var start = chapterRepresentation.workingAudio.totalFrames
        var end = chapterRepresentation.workingAudio.totalFrames

        segments.forEach {
            val verseAudio = AudioFile(it.value)
            end += verseAudio.totalFrames
            val node = VerseNode(start, end)
            nodes.add(node)
            start = end
        }

        onChapterEdited(nodes)
    }
}