package org.wycliffeassociates.otter.common.data.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.Completable
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
    private lateinit var history: NarrationHistory
    private lateinit var serializedHistoryFile: File
    private lateinit var serializedVersesFile: File

    private val splitAudioOnCues = SplitAudioOnCues(directoryProvider)
    private val audioFileUtils = AudioFileUtils(directoryProvider)

    lateinit var workingAudioFile: File
        private set

    lateinit var workingAudio: AudioFile
        private set

    private val verses = mutableListOf<VerseNode>()
    val activeVerses: List<VerseNode>
        get() = verses

    private val ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfSubType("java.util.ArrayDeque")
        .allowIfSubType("java.util.ArrayList")
        .allowIfSubType("org.wycliffeassociates.otter.common.data.narration.")
        .build()
    private val mapper = ObjectMapper().registerKotlinModule()

    init {
        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)

        initializeWorkingAudioFile()
        initializeSerializedHistoryFile()
        initializeVersesFile()

        history = NarrationHistory()
    }

    fun load(chapterFile: File?, forceLoadFromChapterFile: Boolean = false): Completable {
        val chapterFileExists = chapterFile?.exists() ?: false

        val forceLoad = chapterFileExists && forceLoadFromChapterFile
        val narrationEmpty = workingAudio.totalFrames == 0
        val narrationFromChapter = chapterFileExists && narrationEmpty

        return when(forceLoad || narrationFromChapter) {
            true -> createWorkingFilesFromChapterFile(chapterFile!!)
            else -> loadFromSavedVerses()
        }
    }

    fun undo() {
        history.undo()
    }

    fun redo() {
        history.redo()
    }

    fun finalizeVerse(verseIndex: Int = verses.lastIndex) {
        verses.getOrNull(verseIndex)?.end = workingAudio.totalFrames
        //saveHistory()
        saveActiveVerses()
    }

    fun onNextVerse() {
        finalizeVerse()

        val action = NextVerseAction(verses, workingAudio)
        execute(action)
    }

    fun onRecordAgain(verseIndex: Int) {
        val action = RecordAgainAction(verses, workingAudio, verseIndex)
        execute(action)
    }

    fun onVerseMarker(firstVerseIndex: Int, secondVerseIndex: Int, markerPosition: Int) {
        val action = VerseMarkerAction(verses, firstVerseIndex, secondVerseIndex, markerPosition)
        execute(action)
    }

    fun onEditVerse(verseIndex: Int, newStart: Int, newEnd: Int) {
        val action = EditVerseAction(verses, verseIndex, newStart, newEnd)
        execute(action)
    }

    fun onResetAll() {
        val action = ResetAllAction(verses)
        execute(action)
    }

    fun onChapterEdited(newVerses: List<VerseNode>) {
        val action = ChapterEditedAction(verses, newVerses)
        execute(action)
    }

    private fun execute(action: NarrationAction) {
        history.execute(action)
        //saveHistory()
        saveActiveVerses()
    }

    private fun initializeSerializedHistoryFile() {
        serializedHistoryFile = File(projectChapterDir, "$HISTORY_FILE_NAME.json").also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("{}")
            }
        }
    }

    private fun initializeVersesFile() {
        serializedVersesFile = File(projectChapterDir, "${HISTORY_FILE_NAME}_verses.json").also {
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

    private fun saveHistory() {
        val jsonStr = mapper.writeValueAsString(history)
        serializedHistoryFile.writeText(jsonStr)
    }

    private fun saveActiveVerses() {
        val json = ObjectMapper().writeValueAsString(verses)
        serializedVersesFile.writeText(json)
    }

    private fun loadFromSavedVerses(): Completable {
        return Completable.fromCallable {
            val json = serializedVersesFile.readText()
            val mapper = ObjectMapper().registerKotlinModule()

            val reference = object: TypeReference<List<VerseNode>>(){}
            val nodes = mapper.readValue(json, reference)

            verses.addAll(nodes)
        }
    }

    private fun loadFromSavedHistory(): Completable {
        return Completable.fromCallable {
            val ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.ArrayDeque")
                .allowIfSubType("java.util.ArrayList")
                .allowIfSubType("org.wycliffeassociates.otter.common.data.narration.")
                .build()
            val mapper = ObjectMapper().registerKotlinModule()
            mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)

            val jsonStr = serializedHistoryFile.readText()
            mapper.readValue(jsonStr, NarrationHistory::class.java).also {
                history = it
            }
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

        verses.addAll(nodes)

        onChapterEdited(nodes)
    }
}