package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.io.RandomAccessFile
import java.lang.IllegalStateException
import kotlin.math.min

private const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
private const val CHAPTER_NARRATION_FILE_NAME = "chapter_narration.pcm"
private const val DEFAULT_BUFFER_SIZE = 1024

internal class ChapterRepresentation(
    private val workbook: Workbook,
    private val chapter: Chapter
) : AudioFileReader {
    private val logger = LoggerFactory.getLogger(ChapterRepresentation::class.java)

    private var position: Int = 0

    private val frameSizeInBytes: Int
        get() = channels * (workingAudio.bitsPerSample / 8)

    override val sampleRate: Int
        get() = workingAudio.sampleRate

    override val channels: Int
        get() = workingAudio.channels

    override val sampleSize: Int
        get() = workingAudio.bitsPerSample

    override val framePosition: Int
        get() = position / frameSizeInBytes

    override val totalFrames: Int
        get() = activeVerses.sumOf { it.end - it.start }

    val activeVerses = mutableListOf<VerseNode>()

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper().registerKotlinModule()

    val onActiveVersesUpdated = PublishSubject.create<List<VerseNode>>()

    lateinit var workingAudio: OratureAudioFile
        private set

    private var mappedFile: RandomAccessFile? = null

    init {
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()

        open()
    }

    fun loadFromSerializedVerses() {
        val json = serializedVersesFile.readText()
        val reference = object : TypeReference<List<VerseNode>>() {}

        try {
            val nodes = activeVersesMapper.readValue(json, reference)
            activeVerses.clear()
            activeVerses.addAll(nodes)
        } catch (e: JsonMappingException) {
            logger.error("Error in loadFromSerializedVerses: ${e.message}")
        }
    }

    fun finalizeVerse(verseIndex: Int = activeVerses.lastIndex) {
        activeVerses.getOrNull(verseIndex)?.let { verse ->
            val newVerse = VerseNode(verse.start, workingAudio.totalFrames)
            activeVerses[verseIndex] = newVerse
        }
        onVersesUpdated()
    }

    fun onVersesUpdated() {
        serializeVerses()
        sendActiveVerses()
    }

    private fun serializeVerses() {
        val jsonStr = activeVersesMapper.writeValueAsString(activeVerses)
        serializedVersesFile.writeText(jsonStr)
    }

    private fun sendActiveVerses() {
        onActiveVersesUpdated.onNext(activeVerses)
    }

    private fun initializeSerializedVersesFile() {
        val projectChapterDir = workbook.projectFilesAccessor.getChapterAudioDir(workbook, chapter)
        serializedVersesFile = File(projectChapterDir, ACTIVE_VERSES_FILE_NAME).also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }
    }

    private fun initializeWorkingAudioFile() {
        val projectChapterDir = workbook.projectFilesAccessor.getChapterAudioDir(workbook, chapter)
        File(projectChapterDir, CHAPTER_NARRATION_FILE_NAME).also {
            if (!it.exists()) {
                it.createNewFile()
            }
            workingAudio = OratureAudioFile(it)
        }
    }

    override fun hasRemaining(): Boolean {
        return ((totalFrames * frameSizeInBytes) - position) > 0
    }

    private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    override fun getPcmBuffer(bytes: ByteArray): Int {
        var bytesWritten = 0

        mappedFile?.let { raf ->
            for (verse in 0 until activeVerses.size) {
                var verseRead = 0
                val verseStart = activeVerses[verse].start * frameSizeInBytes
                val verseEnd = activeVerses[verse].end * frameSizeInBytes

                val verseLength = verseEnd - verseStart
                raf.seek(verseStart.toLong())

                while (verseRead < verseLength) {
                    val bytesToRead = min(verseLength, buffer.size)
                    val read = raf.read(buffer, 0, bytesToRead)
                    verseRead += read

                    for (i in 0 until read) {
                        if (bytesWritten >= bytes.size) break

                        bytes[bytesWritten] = buffer[i]
                        bytesWritten++
                    }
                }

                if (bytesWritten == bytes.size) break
            }
        } ?: throw IllegalStateException("getPcmBuffer called before opening file")

        position = bytesWritten

        return bytesWritten
    }

    override fun seek(sample: Int) {
        val sampleIndex = sample * frameSizeInBytes
        val limit = totalFrames * frameSizeInBytes
        position = Integer.min(sampleIndex, limit)
    }

    override fun open() {
        mappedFile?.let { release() }
        mappedFile = RandomAccessFile(workingAudio.file, "r")
    }

    override fun release() {
        if (mappedFile != null) {
            mappedFile?.close()
            mappedFile = null
        }
    }

    override fun close() {
        release()
    }
}