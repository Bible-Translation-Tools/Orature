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
import org.wycliffeassociates.otter.common.device.AudioFileReaderProvider
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.log
import kotlin.math.max
import kotlin.math.min

private const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
private const val CHAPTER_NARRATION_FILE_NAME = "chapter_narration.pcm"

internal class ChapterRepresentation(
    private val workbook: Workbook,
    private val chapter: Chapter
) : AudioFileReaderProvider {
    private val openReaderConnections = mutableListOf<ChapterRepresentationConnection>()

    private val logger = LoggerFactory.getLogger(ChapterRepresentation::class.java)

    private val frameSizeInBytes: Int
        get() = channels * (scratchAudio.bitsPerSample / 8)

    private val sampleRate: Int
        get() = scratchAudio.sampleRate

    private val channels: Int
        get() = scratchAudio.channels

    private val sampleSize: Int
        get() = scratchAudio.bitsPerSample

    @get:Synchronized
    val totalFrames: Int
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

    init {
        totalVerses = initalizeActiveVerses()
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()
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
        val end = scratchAudio.totalFrames

        history?.finalizeVerse(end, totalVerses)

        onVersesUpdated()
        return end
    }

    fun onVersesUpdated() {
        serializeVerses()
        publishActiveVerses()
    }

    private fun serializeVerses() {
        val jsonStr = activeVersesMapper.writeValueAsString(activeVerses)
        serializedVersesFile.writeText(jsonStr)
    }

    private fun publishActiveVerses() {
        onActiveVersesUpdated.onNext(
            activeVerses.map {
                val newLoc = absoluteToRelative(it.firstFrame())
                logger.info("Verse ${it.marker.label} absolute loc is ${it.firstFrame()} relative is ${newLoc}")
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
    fun absoluteToRelative(absoluteFrame: Int): Int {
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
            absoluteFrame in node
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

    fun getRangeOfMarker(verse: VerseMarker): IntRange? {
        val verses = activeVerses.map { it }
        if (verses.isEmpty()) return null

        verses
            .find { it.marker.label == verse.label }
            ?.let { verse ->
                return verse.firstFrame()..verse.lastFrame()
            }
        return null
    }

    override fun getAudioFileReader(start: Int?, end: Int?): AudioFileReader {
        val readerConnection = ChapterRepresentationConnection(start, end)
        synchronized(openReaderConnections) {
            openReaderConnections.add(readerConnection)
        }
        return readerConnection
    }

    fun closeConnections() {
        val temp = synchronized(openReaderConnections) {
            openReaderConnections.map { it }
        }
        temp.forEach {
            it.close()
            it.release()
        }
    }

    inner class ChapterRepresentationConnection(
        var start: Int? = null,
        var end: Int?
    ) : AudioFileReader {
        override val sampleRate: Int = this@ChapterRepresentation.sampleRate
        override val channels: Int = this@ChapterRepresentation.channels
        override val sampleSizeBits: Int = this@ChapterRepresentation.sampleSize

        private var randomAccessFile: RandomAccessFile? = null

        private var _position = start?.times(frameSizeInBytes) ?: 0
        private var position: Int
            get() = _position
            set(value) {
                if (_position !in 0..(totalFrames * frameSizeInBytes)) {
                    logger.warn("setting position outside of total frames?")
                }
                _position = value
            }

        private val CHAPTER_UNLOCKED: Int = -1
        private val lockToVerse = AtomicInteger(CHAPTER_UNLOCKED)

        @Synchronized
        fun lockToVerse(index: Int?) {
            if (index != null) {
                if (index > activeVerses.lastIndex || index < 0) return

                val node = activeVerses.get(index)
                lockToVerse.set(index)
                if (position !in node) {
                    // Is this a good default? This is if the position was out of the verse
                    position = node.firstFrame() * frameSizeInBytes
                }
            } else {
                lockToVerse.set(CHAPTER_UNLOCKED)
            }
        }

        @Synchronized
        fun reset() {
            val verses = activeVerses
            if (verses.isEmpty()) position = 0

            var index = lockToVerse.get()
            index = if (index == CHAPTER_UNLOCKED) 0 else index
            position = verses.get(index).firstFrame() * frameSizeInBytes
        }

        @get:Synchronized
        override val framePosition: Int
            get() = position / frameSizeInBytes

        @get:Synchronized
        override val totalFrames: Int
            get() {
                val lockedVerse = lockToVerse.get()
                return if (lockedVerse == CHAPTER_UNLOCKED) {
                    this@ChapterRepresentation.totalFrames
                } else {
                    activeVerses.get(lockedVerse).length
                }
            }

        @Synchronized
        override fun hasRemaining(): Boolean {
            if (totalFrames == 0) return false

            val verses = activeVerses
            val verseIndex = lockToVerse.get()
            val hasRemaining =  if (verseIndex != CHAPTER_UNLOCKED) {
                framePosition in verses[verseIndex] && framePosition != verses[verseIndex].lastFrame()
            } else {
                verses.any { framePosition in it } && framePosition != verses.last().lastFrame()
            }
            if (!hasRemaining) {
                logger.info("No audio remaining")
            }
            return hasRemaining
        }

        override fun supportsTimeShifting(): Boolean {
            return false
        }

        private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        @Synchronized
        override fun getPcmBuffer(bytes: ByteArray): Int {
            if (totalFrames == 0) return 0



            return when (lockToVerse.get()) {
                CHAPTER_UNLOCKED -> getPcmBufferChapter(bytes)
                else -> getPcmBufferVerse(bytes, activeVerses[lockToVerse.get()])
            }
        }

        @Synchronized
        private fun getPcmBufferChapter(bytes: ByteArray): Int {
            val verses = activeVerses
            if (verses.isEmpty()) {
                logger.info("Playing a chapter but the verses are empty?")
                return 0
            }

            val verseToReadFrom = getVerseToReadFrom(verses)
            return if (verseToReadFrom != null) {
                getPcmBufferVerse(bytes, verseToReadFrom)
            } else 0
        }

        private fun getVerseToReadFrom(verses: List<VerseNode>): VerseNode? {
            var currentVerseIndex = verses.indexOfFirst { framePosition in it }
            if (currentVerseIndex == -1) {
                currentVerseIndex = 0
            }

            val verse = verses.getOrNull(currentVerseIndex)
            return verse
        }

        @Synchronized
        private fun getPcmBufferVerse(bytes: ByteArray, verse: VerseNode): Int {
            var bytesWritten = 0
            if (randomAccessFile == null) {
                logger.error("Should have opened the file, this is weird")
                open()
            }
            val raf = randomAccessFile!!

            if (framePosition !in verse) {
                return 0
            }

            var framesToRead = min(bytes.size / frameSizeInBytes, verse.length)

            // if there is a negative frames to read or
            if (framesToRead <= 0 || framePosition !in verse) {
                logger.error("Frames to read is negative: $framePosition, $framesToRead, ${verse.marker.formattedLabel}")
                position = verse.lastFrame() * frameSizeInBytes
                return 0
            }

            val sectors = verse.getSectorsFromOffset(framePosition, framesToRead)

            if (sectors.isEmpty()) {
                logger.error("sectors is empty for verse ${verse.marker.label}")
                position = verse.lastFrame() * frameSizeInBytes
                return 0
            }

            for (sector in sectors) {
                if (framesToRead <= 0 || framePosition !in verse) break

                val framesToCopyFromSector = max(min(framesToRead, sector.length()), 0)

                val seekLoc = (sector.first * frameSizeInBytes).toLong()
                raf.seek(seekLoc)
                val temp = ByteArray(framesToCopyFromSector * frameSizeInBytes)
                val toCopy = raf.read(temp)
                try {
                    System.arraycopy(temp, 0, bytes, bytesWritten, toCopy)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    logger.error("arraycopy out of bounds, $bytesWritten bytes written, $toCopy toCopy", e)
                }
                bytesWritten += toCopy
                position += toCopy
                framesToRead -= toCopy / frameSizeInBytes

                if (framePosition !in sector) {
                    logger.warn("Frame position popped out of sector; it got corrected, but there must be a math issue. framePosition:$framePosition, last:${sector.last}")
                    position = sector.last * frameSizeInBytes
                }
            }

            if (framePosition == verse.lastFrame()) {
                adjustPositionToNextVerse(verse)
            }

            return bytesWritten
        }

        private fun adjustPositionToNextSector(verse: VerseNode, sector: IntRange, sectors: List<IntRange>) {
            if (framePosition != sector.last) return

            val sectorIndex = sectors.indexOf(sector)
            if (sectorIndex == sectors.lastIndex) {
                adjustPositionToNextVerse(verse)
            } else {
                position = sectors[sectorIndex + 1].first * frameSizeInBytes
            }
        }

        private fun adjustPositionToNextVerse(verse: VerseNode) {
            if (lockToVerse.get() != CHAPTER_UNLOCKED) return

            val verses = activeVerses
            val index = verses.indexOf(verse)
            if (index == verses.lastIndex) return

            position = verses[index + 1].firstFrame() * frameSizeInBytes
        }

        @Synchronized
        override fun seek(sample: Int) {
            position = when {
//                sample <= startBounds -> startBounds * frameSizeInBytes
//                sample >= endBounds -> endBounds - 1 * frameSizeInBytes
                else -> {
                    sample * frameSizeInBytes
                }
            }
        }

        override fun open() {
            randomAccessFile?.let { release() }
            randomAccessFile = RandomAccessFile(scratchAudio.file, "r")
            logger.info("open called")
        }

        override fun release() {
            if (randomAccessFile != null) {
                randomAccessFile?.close()
                randomAccessFile = null
            }
            openReaderConnections.remove(this)
        }

        override fun close() {
            release()
        }
    }
}