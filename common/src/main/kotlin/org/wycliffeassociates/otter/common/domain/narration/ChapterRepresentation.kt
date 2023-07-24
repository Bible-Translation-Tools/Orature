package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.primitives.VerseNode
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer

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
        get() = activeVerses.sumOf { it.start + it.end }

    val activeVerses = mutableListOf<VerseNode>()

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper().registerKotlinModule()

    val onActiveVersesUpdated = PublishSubject.create<List<VerseNode>>()

    lateinit var workingAudio: AudioFile
        private set

    init {
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()
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
            workingAudio = AudioFile(it)
        }
    }

    override fun hasRemaining(): Boolean {
        return (totalFrames - position) > 0
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        var bytesWritten = 0

        RandomAccessFile(workingAudio.file, "r").use { raf ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            val byteBuffer = ByteBuffer.wrap(bytes)

            for (verse in activeVerses) {
                var verseRead = 0
                val start = verse.start * frameSizeInBytes
                val end = verse.end * frameSizeInBytes

                val length = end - start
                raf.seek(start.toLong())

                while (verseRead < length) {
                    val read = raf.read(buffer)
                    verseRead += read
                    bytesWritten += read

                    if (bytesWritten > bytes.size) {
                        val diff = bytesWritten - bytes.size
                        val smallerBuffer = buffer.copyOf(buffer.size - diff)
                        byteBuffer.put(smallerBuffer)
                        break
                    } else {
                        byteBuffer.put(buffer)
                    }
                }

                if (bytesWritten == bytes.size) break
            }
        }

        position = bytesWritten

        return bytesWritten
    }

    override fun seek(sample: Int) {
        position = sample
    }

    override fun open() {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}