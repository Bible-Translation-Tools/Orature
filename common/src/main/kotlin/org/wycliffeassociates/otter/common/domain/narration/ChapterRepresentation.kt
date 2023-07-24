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
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.lang.IllegalStateException
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

    override fun getPcmBuffer(bytes: ByteArray): Int {
        var bytesWritten = 0

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val byteBuffer = ByteBuffer.wrap(bytes)

        mappedFile?.let { raf ->
            for (i in 0 until activeVerses.size) {
                var verseRead = 0
                val start = activeVerses[i].start * frameSizeInBytes
                val end = activeVerses[i].end * frameSizeInBytes

                val length = end - start
                raf.seek(start.toLong())

                while (verseRead < length) {
                    val read = raf.read(buffer)
                    verseRead += read
                    bytesWritten += read

                    if (bytesWritten > bytes.size) {
                        // If bytes read exceed target byte array size,
                        // we need to write only the part that fits in the array
                        val diff = bytesWritten - bytes.size
                        val partialSize = buffer.size - diff

                        for (j in 0 until partialSize) {
                            byteBuffer.put(buffer[j])
                        }
                        break
                    } else {
                        byteBuffer.put(buffer)
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
            try {
                // https://stackoverflow.com/questions/25238110/how-to-properly-close-mappedbytebuffer/25239834#25239834
                // TODO: Replace with https://docs.oracle.com/en/java/javase/14/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#ofByteBuffer(java.nio.ByteBuffer)
                val unsafeClass = Class.forName("sun.misc.Unsafe")
                val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
                unsafeField.isAccessible = true
                val unsafe: Any = unsafeField.get(null)
                val invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer::class.java)
                invokeCleaner.invoke(unsafe, mappedFile)
            } catch (e: Exception) {

            }
            mappedFile?.channel?.close()
            mappedFile?.close()
            mappedFile = null
            System.gc()
        }
    }

    override fun close() {
        release()
    }
}