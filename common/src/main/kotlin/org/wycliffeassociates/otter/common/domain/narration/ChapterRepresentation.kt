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
        get() = activeVerses.sumOf { it.length }

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

    fun finalizeVerse(verseIndex: Int, history: NarrationHistory? = null): Int {
        logger.info("Finalizing verse: ${verseIndex}")
        val end = scratchAudio.totalFrames

        history?.finalizeVerse(end, totalVerses)

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
        var verse = findVerse(absoluteFrame)
        verse?.let {
            val index = verses.indexOf(verse)
            var rel = 0
            for (idx in 0 until index) {
                rel += verses[idx].length
            }
            rel += it.framesToPosition(absoluteFrame)
            return rel
        }
        return 0
    }

    private fun findVerse(absoluteFrame: Int): VerseNode? {
        return activeVerses.find { node ->
            val absoluteIsInRange = absoluteFrame in node
            val absoluteIsEndOfFile = absoluteFrame == node.lastFrame() && absoluteFrame == activeVerses.last().lastFrame()
            absoluteIsInRange || absoluteIsEndOfFile
        }
    }

    /**
     * Converts a relative index (audio only taking into account the currently active verses)
     * to an absolute position into the scratch audio file. This conversion is performed by counting frames through
     * the range of each active verse.
     */
    internal fun relativeToAbsolute(relativeIdx: Int): Int {
        var remaining = relativeIdx
        activeVerses.forEach { node ->
            val range = node.length
            if (range > remaining) {
                remaining -= range
            } else {
                return node.absoluteFrameFromOffset(remaining)
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
            var framesToRead = bytes.size / frameSizeInBytes
            val verses = activeVerses
            val startingVerse = findVerse(framePosition)
            startingVerse?.let { startingVerse ->
                val startIndex = verses.indexOf(startingVerse)
                for (verseIdx in startIndex until verses.size) {
                    val verse = verses[verseIdx]
                    val sectors = verse.getSectorsFromOffset(framePosition, framesToRead)
                    val framesTaken = sectors.sumOf { it.length() }
                    for (sector in sectors) {
                        raf.seek((sector.first * frameSizeInBytes).toLong())
                        val temp = ByteArray(framesTaken * frameSizeInBytes)
                        val toCopy = raf.read(temp)
                        System.arraycopy(temp, 0, bytes, bytesWritten, toCopy)
                        bytesWritten += toCopy
                        position += toCopy
                        framesToRead -= toCopy / frameSizeInBytes
                    }
                }
            }
        } ?: throw IllegalStateException("getPcmBuffer called before opening file")

        return bytesWritten
    }

    @Synchronized
    override fun seek(sample: Int) {
        val scratchFileLength = scratchAudio.totalFrames * frameSizeInBytes
        when {
            sample <= 0 -> this.position = 0
            sample >= totalFrames -> this.position = scratchFileLength
            else -> {
                val absoluteFrame = relativeToAbsolute(sample)
                this.position = max(min(absoluteFrame * frameSizeInBytes, scratchFileLength), 0)
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
                val end = verse.endScratchFrame
                return start..end
            }
        return null
    }
}