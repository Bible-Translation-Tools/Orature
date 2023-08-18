package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.io.RandomAccessFile
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min

private const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
private const val CHAPTER_NARRATION_FILE_NAME = "chapter_narration.pcm"

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

    @get:Synchronized
    override val framePosition: Int
        get() = position / frameSizeInBytes

    @get:Synchronized
    override val totalFrames: Int
        get() = activeVerses.sumOf { it.end - it.start }

    @get:Synchronized
    internal val activeVerses: List<VerseNode>
        get() = totalVerses.filter {
            it.placed
        }

    internal val totalVerses: MutableList<VerseNode>

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper().registerKotlinModule()

    val onActiveVersesUpdated = PublishSubject.create<List<VerseMarker>>()

    lateinit var workingAudio: OratureAudioFile
        private set

    private var randomAccessFile: RandomAccessFile? = null

    init {
        totalVerses = initalizeActiveVerses()
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()

        open()
    }

    private fun initalizeActiveVerses(): MutableList<VerseNode> {
        return chapter
            .getDraft()
            .map { chunk ->
                VerseMarker(chunk.start, chunk.end, 0)
            }
            .map { marker ->
                VerseNode(0, 0, false, marker)
            }
            .toList()
            .blockingGet()
    }

    fun loadFromSerializedVerses() {
        val json = serializedVersesFile.readText()
        val reference = object : TypeReference<List<VerseNode>>() {}

        try {
            val nodes = activeVersesMapper.readValue(json, reference)
            totalVerses.forEach { it.clear() }
            totalVerses.forEachIndexed { idx, _ ->
                nodes.getOrNull(idx)?.let { totalVerses[idx] = it }
            }
        } catch (e: JsonMappingException) {
            logger.error("Error in loadFromSerializedVerses: ${e.message}")
        }
    }

    fun finalizeVerse(verseIndex: Int = activeVerses.lastIndex) {
        activeVerses.getOrNull(verseIndex)?.end = workingAudio.totalFrames
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
        onActiveVersesUpdated.onNext(
            activeVerses.map {
                val newLoc = absoluteToRelative(it.start)
                it.marker.copy(location = newLoc)
            }
        )
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

    private fun absoluteToRelative(absolute: Int): Int {
        val verse = activeVerses.find { absolute in it.start..it.end }
        verse?.let {
            val index = activeVerses.indexOf(verse)
            var rel = 0
            for (idx in 0..index) {
                rel += activeVerses[idx].end - activeVerses[idx].start
            }
            rel += absolute - it.start
            return rel
        }
        return 0
    }

    internal fun relativeToAbsolute(relativeIdx: Int): Int {
        var remaining = relativeIdx
        activeVerses.forEach {
            val range = it.end - it.start
            if (range > remaining) {
                remaining -= range
            } else {
                return it.start + min(remaining, 0)
            }
        }
        return remaining
    }

    @Synchronized
    override fun hasRemaining(): Boolean {
        return ((totalFrames * frameSizeInBytes) - position) > 0
    }

    private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    @Synchronized
    override fun getPcmBuffer(bytes: ByteArray): Int {
        var bytesWritten = 0

        randomAccessFile?.let { raf ->
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

    @Synchronized
    override fun seek(sample: Int) {
        var pos = 0

        for (i in 0 until activeVerses.size) {
            val verse = activeVerses[i]
            val verseRange = verse.end - verse.start

            // jump by the verse range if it combined with our accumulated position is still less than the seek point
            if (sample > pos + verseRange) {
                pos += verseRange
            } else {
                // we've found the range the seek position falls within, so get the delta and add it to the start
                this.position = min((sample - pos) + verse.start, this.totalFrames) * frameSizeInBytes
                return
            }
        }
    }

    override fun open() {
        randomAccessFile?.let { release() }
        randomAccessFile = RandomAccessFile(workingAudio.file, "r")
    }

    override fun release() {
        if (randomAccessFile != null) {
            randomAccessFile?.close()
            randomAccessFile = null
        }
    }

    override fun close() {
        release()
    }

    fun getRangeOfMarker(verse: VerseMarker): IntRange? {
        val verses = activeVerses
        if (verses.isEmpty()) return null

        verses
            .find { it.marker.label == verse.label }
            ?.let {
                val start = absoluteToRelative(it.start)
                var end = 0
                val index = verses.indexOf(it)
                if (verses.lastIndex != index) {
                    val next = verses[index + 1]
                    end = max(absoluteToRelative(next.start) - 1, 0)
                } else {
                    absoluteToRelative(verses.last().end)
                }
                return start..end
            }
        return null
    }
}