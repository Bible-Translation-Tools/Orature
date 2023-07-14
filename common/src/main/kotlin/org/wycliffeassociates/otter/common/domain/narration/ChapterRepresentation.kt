package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import java.io.File

private const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
private const val WORKING_CHAPTER_FILE_NAME = "working_chapter.pcm"

internal class ChapterRepresentation(
    private val projectFilesAccessor: ProjectFilesAccessor,
    private val workbook: Workbook,
    private val chapter: Chapter
) {
    val verses = mutableListOf<VerseNode>()

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper().registerKotlinModule()

    lateinit var workingAudio: AudioFile
        private set

    init {
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()
    }

    private fun initializeSerializedVersesFile() {
        val projectChapterDir = projectFilesAccessor.getChapterAudioDir(workbook, chapter)
        serializedVersesFile = File(projectChapterDir, ACTIVE_VERSES_FILE_NAME).also {
            if (!it.exists()) {
                it.createNewFile()
                it.writeText("[]")
            }
        }
    }

    private fun initializeWorkingAudioFile() {
        val projectChapterDir = projectFilesAccessor.getChapterAudioDir(workbook, chapter)
        File(projectChapterDir, WORKING_CHAPTER_FILE_NAME).also {
            if (!it.exists()) {
                it.createNewFile()
            }
            workingAudio = AudioFile(it)
        }
    }

    fun serializeVerses() {
        val jsonStr = activeVersesMapper.writeValueAsString(verses)
        serializedVersesFile.writeText(jsonStr)
    }

    fun loadFromSerializedVerses() {
        val json = serializedVersesFile.readText()
        val reference = object : TypeReference<List<VerseNode>>() {}
        val nodes = activeVersesMapper.readValue(json, reference)
        verses.addAll(nodes)
    }
}