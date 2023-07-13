package org.wycliffeassociates.otter.common.data.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.domain.narration.AudioFileUtils
import org.wycliffeassociates.otter.common.domain.narration.SplitAudioOnCues
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

private const val HISTORY_FILE_NAME = "narration"

class Narration (
    private val directoryProvider: IDirectoryProvider,
    private val projectChapterDir: File
) {
    private val history = NarrationHistory()
    private lateinit var serializedVersesFile: File

    private val splitAudioOnCues = SplitAudioOnCues(directoryProvider)
    private val audioFileUtils = AudioFileUtils(directoryProvider)

    lateinit var workingAudioFile: File
        private set

    lateinit var workingAudio: AudioFile
        private set

    private val verses = mutableListOf<VerseNode>()

    private val mapper = ObjectMapper().registerKotlinModule()

    val activeVerses = PublishSubject.create<List<VerseNode>>()
    val hasUndo = PublishSubject.create<Boolean>()
    val hasRedo = PublishSubject.create<Boolean>()

    init {
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()
    }

    fun load(chapterFile: File?, forceLoadFromChapterFile: Boolean = false): Completable {
        val chapterFileExists = chapterFile?.exists() ?: false

        val forceLoad = chapterFileExists && forceLoadFromChapterFile
        val narrationEmpty = workingAudio.totalFrames == 0
        val narrationFromChapter = chapterFileExists && narrationEmpty

        return when(forceLoad || narrationFromChapter) {
            true -> createWorkingFilesFromChapterFile(chapterFile!!)
            else -> loadFromSerializedVerses()
        }
    }

    fun undo() {
        history.undo(verses)

        sendHistoryHasActions()
        sendActiveVerses()
        serializeVerses()
    }

    fun redo() {
        history.redo(verses)

        sendHistoryHasActions()
        sendActiveVerses()
        serializeVerses()
    }

    fun finalizeVerse(verseIndex: Int = verses.lastIndex) {
        verses.getOrNull(verseIndex)?.end = workingAudio.totalFrames
        serializeVerses()
    }

    fun onNewVerse() {
        val action = NewVerseAction()
        execute(action)
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verseIndex)
        execute(action)
    }

    fun onVerseMarker(firstVerseIndex: Int, secondVerseIndex: Int, markerPosition: Int) {
        val action = VerseMarkerAction(firstVerseIndex, secondVerseIndex, markerPosition)
        execute(action)
    }

    fun onEditVerse(verseIndex: Int, newStart: Int, newEnd: Int) {
        val action = EditVerseAction(verseIndex, newStart, newEnd)
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

    private fun execute(action: NarrationAction) {
        history.execute(action, verses, workingAudio)

        sendHistoryHasActions()
        sendActiveVerses()
        serializeVerses()
    }

    private fun initializeSerializedVersesFile() {
        serializedVersesFile = File(projectChapterDir, "$HISTORY_FILE_NAME.json").also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("[]")
            }
        }
    }

    private fun initializeWorkingAudioFile() {
        workingAudioFile = File(projectChapterDir, "$HISTORY_FILE_NAME.pcm").also {
            if (!it.exists()) {
                it.createNewFile()
            }
            workingAudio = AudioFile(it)
        }
    }

    private fun serializeVerses() {
        val jsonStr = mapper.writeValueAsString(verses)
        serializedVersesFile.writeText(jsonStr)
    }

    private fun loadFromSerializedVerses(): Completable {
        return Completable.fromCallable {
            val json = serializedVersesFile.readText()
            val reference = object: TypeReference<List<VerseNode>>(){}
            val nodes = mapper.readValue(json, reference)
            verses.addAll(nodes)

            sendActiveVerses()
        }
    }

    private fun createWorkingFilesFromChapterFile(file: File): Completable {
        return splitAudioOnCues.execute(file)
            .flatMapCompletable { cues ->
                Completable.fromCallable {
                    createVersesFromCues(cues)
                    appendCuesToWorkingAudio(cues)
                }
            }
    }

    private fun appendCuesToWorkingAudio(cues: Map<String, File>) {
        cues.forEach {
            audioFileUtils.appendFile(workingAudio, it.value)
        }
    }

    private fun createVersesFromCues(cues: Map<String, File>) {
        val nodes = mutableListOf<VerseNode>()
        var start = workingAudio.totalFrames
        var end = workingAudio.totalFrames

        cues.forEach {
            val verseAudio = AudioFile(it.value)
            end += verseAudio.totalFrames
            val node = VerseNode(start, end)
            nodes.add(node)
            start = end
        }

        onChapterEdited(nodes)
    }

    private fun sendHistoryHasActions() {
        hasUndo.onNext(history.hasUndo())
        hasRedo.onNext(history.hasRedo())
    }

    private fun sendActiveVerses() {
        activeVerses.onNext(verses)
    }
}