package org.wycliffeassociates.otter.common.domain.narration

import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import java.io.File

class Narration private constructor(directoryProvider: IDirectoryProvider,) {
    private val history = NarrationHistory()

    private val splitAudioOnCues = SplitAudioOnCues(directoryProvider)
    private val audioFileUtils = AudioFileUtils(directoryProvider)

    private lateinit var chapterRepresentation: ChapterRepresentation

    val activeVerses: List<VerseNode>
        get() = chapterRepresentation.verses
    val onActiveVersesUpdated = PublishSubject.create<List<VerseNode>>()

    private var recorder: IAudioRecorder? = null
    private var writer: WavFileWriter? = null

    companion object {
        fun load(
            workbook: Workbook,
            chapter: Chapter,
            directoryProvider: IDirectoryProvider,
            projectFilesAccessor: ProjectFilesAccessor,
            recorder: IAudioRecorder,
            forceLoadFromChapterFile: Boolean = false
        ): Narration {
            return Narration(directoryProvider).apply {
                chapterRepresentation = ChapterRepresentation(
                    projectFilesAccessor,
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

                initializeRecorder(recorder)
                sendActiveVerses()
            }
        }
    }

    fun initializeRecorder(recorder: IAudioRecorder) {
        this.recorder = recorder
        writer = WavFileWriter(
            chapterRepresentation.workingAudio,
            recorder.getAudioStream(),
            true
        ) {
            /* no op */
        }
    }

    fun closeRecorder() {
        recorder?.stop()
        recorder = null

        writer?.writer?.dispose()
        writer = null
    }

    fun undo() {
        history.undo(chapterRepresentation.verses)

        sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    fun redo() {
        history.redo(chapterRepresentation.verses)

        sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    fun finalizeVerse(verseIndex: Int = chapterRepresentation.verses.lastIndex) {
        chapterRepresentation.verses.getOrNull(verseIndex)?.end =
            chapterRepresentation.workingAudio.totalFrames
        chapterRepresentation.serializeVerses()
    }

    fun onNewVerse() {
        val action = NewVerseAction()
        execute(action)

        recorder?.start()
        writer?.start()
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verseIndex)
        execute(action)

        recorder?.start()
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

    fun pauseRecording(verseIndex: Int = chapterRepresentation.verses.lastIndex) {
        recorder?.pause()
        writer?.pause()

        finalizeVerse(verseIndex)
    }

    fun resumeRecording() {
        recorder?.start()
        writer?.start()
    }

    fun hasUndo(): Boolean {
        return history.hasUndo()
    }

    fun hasRedo(): Boolean {
        return history.hasRedo()
    }

    fun getWorkingAudio(): AudioFile {
        return chapterRepresentation.workingAudio
    }

    private fun execute(action: NarrationAction) {
        history.execute(action, chapterRepresentation.verses, chapterRepresentation.workingAudio)

        sendActiveVerses()
        chapterRepresentation.serializeVerses()
    }

    private fun createWorkingFilesFromChapterFile(file: File) {
        val cues = splitAudioOnCues.execute(file)
        createVersesFromCues(cues)
        appendCuesToWorkingAudio(cues)
    }

    private fun appendCuesToWorkingAudio(cues: Map<String, File>) {
        cues.forEach {
            audioFileUtils.appendFile(chapterRepresentation.workingAudio, it.value)
        }
    }

    private fun createVersesFromCues(cues: Map<String, File>) {
        val nodes = mutableListOf<VerseNode>()
        var start = chapterRepresentation.workingAudio.totalFrames
        var end = chapterRepresentation.workingAudio.totalFrames

        cues.forEach {
            val verseAudio = AudioFile(it.value)
            end += verseAudio.totalFrames
            val node = VerseNode(start, end)
            nodes.add(node)
            start = end
        }

        onChapterEdited(nodes)
    }

    private fun sendActiveVerses() {
        onActiveVersesUpdated.onNext(chapterRepresentation.verses)
    }
}