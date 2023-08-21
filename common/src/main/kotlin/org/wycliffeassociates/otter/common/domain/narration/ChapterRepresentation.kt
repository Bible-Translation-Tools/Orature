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
        get() = channels * (scratchAudio.bitsPerSample / 8)

    override val sampleRate: Int
        get() = scratchAudio.sampleRate

    override val channels: Int
        get() = scratchAudio.channels

    override val sampleSize: Int
        get() = scratchAudio.bitsPerSample

    @get:Synchronized
    override val framePosition: Int
        get() = position / frameSizeInBytes

    @get:Synchronized
    override val totalFrames: Int
        get() = activeVerses.sumOf { it.endScratchFrame - it.startScratchFrame }

    @get:Synchronized
    internal val activeVerses: List<VerseNode>
        get() = totalVerses.filter { it.placed }

    internal val totalVerses: MutableList<VerseNode>

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper().registerKotlinModule()

    val onActiveVersesUpdated = PublishSubject.create<List<VerseMarker>>()

    // Represents an ever growing tape of audio. This tape may have "dirty" sectors corresponding to outdated
    // content, which needs to be removed before finalizing the audio.
    lateinit var scratchAudio: OratureAudioFile
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

    fun finalizeVerse(verseIndex: Int): Int {
        logger.info("Finalizing verse: ${verseIndex}")
        val end = scratchAudio.totalFrames
        activeVerses.getOrNull(verseIndex)?.endScratchFrame = end
        onVersesUpdated()
        return end
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
                val newLoc = absoluteToRelative(it.startScratchFrame)
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
            scratchAudio = OratureAudioFile(it)
        }
    }

    /**
     * Converts the absolute audio frame position within the scratch audio file to a "relative" position as if the
     * audio only contained the segments referrenced by the active verse nodes.
     */
    private fun absoluteToRelative(absoluteFrame: Int): Int {
        val verses = activeVerses
        var verse = verses.find {
            val absoluteIsInRange = absoluteFrame in it.startScratchFrame until it.endScratchFrame
            val absoluteIsAbsoluteEnd = absoluteFrame == it.endScratchFrame && absoluteFrame == activeVerses.last().endScratchFrame
            absoluteIsInRange || absoluteIsAbsoluteEnd
        }
        verse?.let {
            val index = verses.indexOf(verse)
            var rel = 0
            for (idx in 0 until index) {
                rel += verses[idx].endScratchFrame - verses[idx].startScratchFrame
            }
            rel += absoluteFrame - it.startScratchFrame
            return rel
        }
        return 0
    }

    /**
     * Converts a relative index (audio only taking into account the currently active verses)
     * to an absolute position into the scratch audio file. This conversion is performed by counting frames through
     * the range of each active verse.
     */
    internal fun relativeToAbsolute(relativeIdx: Int): Int {
        var remaining = relativeIdx
        activeVerses.forEach {
            val range = it.endScratchFrame - it.startScratchFrame
            if (range > remaining) {
                remaining -= range
            } else {
                return it.startScratchFrame + min(remaining, 0)
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
                val verseStart = activeVerses[verse].startScratchFrame * frameSizeInBytes
                val verseEnd = activeVerses[verse].endScratchFrame * frameSizeInBytes

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
            val verseRange = verse.endScratchFrame - verse.startScratchFrame

            // jump by the verse range if it combined with our accumulated position is still less than the seek point
            if (sample > pos + verseRange) {
                pos += verseRange
            } else {
                // we've found the range the seek position falls within, so get the delta and add it to the start
                this.position = min((sample - pos) + verse.startScratchFrame, this.totalFrames) * frameSizeInBytes
                return
            }
        }
    }

    override fun open() {
        randomAccessFile?.let { release() }
        randomAccessFile = RandomAccessFile(scratchAudio.file, "r")
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
        val verses = activeVerses.map { it }
        if (verses.isEmpty()) return null

        verses
            .find { it.marker.label == verse.label }
            ?.let { verse ->
                val start = verse.startScratchFrame
                var end = 0
                val index = verses.indexOf(verse)
                if (verses.lastIndex != index) {
                    val next = verses[index + 1]
                    end = max(next.startScratchFrame - 1, 0)
                } else {
                    end = verses.last().endScratchFrame
                }
                return start..end
            }
        return null
    }
}