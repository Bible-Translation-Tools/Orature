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
import java.lang.IllegalStateException
import kotlin.math.max
import kotlin.math.min

private const val ACTIVE_VERSES_FILE_NAME = "active_verses.json"
private const val CHAPTER_NARRATION_FILE_NAME = "chapter_narration.pcm"

internal class ChapterRepresentation(
    private val workbook: Workbook,
    private val chapter: Chapter
) : AudioFileReaderProvider {
    private val openReaderConnections = mutableListOf<ChapterRepresentationConnection>()

    //private val // logger = LoggerFactory.getLogger(ChapterRepresentation::class.java)

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
        // logger.info(json)
        val reference = object : TypeReference<List<VerseNode>>() {}

        try {
            val nodes = activeVersesMapper.readValue(json, reference)
            totalVerses.forEach { it.clear() }
            totalVerses.forEachIndexed { idx, _ ->
                nodes.getOrNull(idx)?.let { totalVerses[idx] = it }
            }
        } catch (e: JsonMappingException) {
            // logger.error("Error in loadFromSerializedVerses: ${e.message}")
        }
    }

    fun finalizeVerse(verseIndex: Int, history: NarrationHistory? = null): Int {
        // logger.info("Finalizing verse: ${verseIndex}")
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
                val newLoc = absoluteToRelative(it.firstFrame())
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
            val absoluteIsInRange = absoluteFrame in node && absoluteFrame != node.lastFrame()
            val absoluteIsEndOfFile =
                absoluteFrame == node.lastFrame() && absoluteFrame == activeVerses.last().lastFrame()
            absoluteIsInRange || absoluteIsEndOfFile
        }
    }

    /**
     * if the frame is the end frame of a verse, and there are more verses, return the frame of the next verse
     */
    private fun jumpGap(absoluteFrame: Int): Int {
        val verses = activeVerses
        val index = verses.indexOfFirst { node ->
            absoluteFrame == node.lastFrame()
        }
        when {
            verses.isEmpty() -> return 0
            index == -1 -> {
                return if (absoluteFrame == 0) {
                    // if 0, assume playback should go to the first verse (might not be the smallest frame)
                    verses.first().firstFrame()
                } else {
                    // logger.warn("In jump gap, moving to closest starting frame for frame: $absoluteFrame, but this is unexpected")
                    verses.minBy { it.firstFrame() - absoluteFrame }.firstFrame()
                }
            }
            index == verses.lastIndex -> return 0
            else -> {
                for (i in index + 1 until verses.size) {
                    if (verses[i].sectors.isNotEmpty()) {
                        return verses[i].firstFrame()
                    }
                }
                return 0
            }
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
        override val sampleSize: Int = this@ChapterRepresentation.sampleSize

        private var randomAccessFile: RandomAccessFile? = null

        private var _position = start?.times(frameSizeInBytes) ?: 0
        private var position: Int
            get() = _position
            set(value) {
                _position = if (value < startBounds * frameSizeInBytes || value > endBounds * frameSizeInBytes) {
                    // logger.error("tried to set a position outside bounds")
                    value.coerceIn(startBounds * frameSizeInBytes, endBounds * frameSizeInBytes)
                } else {
                    value
                }
            }

        private val startBounds: Int
            inline get() {
                return when {
                    start != null -> start!!
                    activeVerses.isEmpty() -> 0
                    else -> activeVerses.minBy { it.firstFrame() }.firstFrame()
                }
            }

        private val endBounds: Int
            inline get() {
                return when {
                    end != null -> end!!
                    activeVerses.isEmpty() -> scratchAudio.totalFrames
                    else -> activeVerses.maxBy { it.lastFrame() }.lastFrame()
                }
            }

        @get:Synchronized
        override val framePosition: Int
            get() = position / frameSizeInBytes

        override val totalFrames: Int
            get() {
                return if (start == null && end == null) {
                    this@ChapterRepresentation.totalFrames
                } else {
                    end!! - start!!
                }
            }

        @Synchronized
        override fun hasRemaining(): Boolean {
            val remaining = endBounds - framePosition > 0
            // logger.info("hasRemaining is ${remaining} for ${end ?: totalFrames}, frame position is ${framePosition}")
            return remaining
        }

        override fun supportsTimeShifting(): Boolean {
            return false
        }

        private val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

        @Synchronized
        override fun getPcmBuffer(bytes: ByteArray): Int {
            var bytesWritten = 0
            if (randomAccessFile == null) {
                // logger.error("Should have opened the file, this is weird")
                open()
            }

            val raf = randomAccessFile!!
            val bounds = startBounds until endBounds

            if (framePosition !in bounds) {
                when {
                    framePosition < bounds.first -> position = bounds.first * frameSizeInBytes
                    else -> position = bounds.last * frameSizeInBytes
                }
            }

            var framesToRead = min(bytes.size / frameSizeInBytes, endBounds - startBounds)
            val verses = activeVerses
            var startingVerse = findVerse(framePosition)

            while (startingVerse == null) {
                val moveTo = jumpGap(framePosition)
                if (moveTo == 0) break
                seek(moveTo)
                startingVerse = findVerse(framePosition)
            }

            // logger.info("Starting verse is ${startingVerse}")
            startingVerse?.let { startingVerse ->
                var startIndex = verses.indexOf(startingVerse)
                if (startIndex == -1) {
                    // logger.error("Aborting getPCMBuffer, could not find verse for frame Position: $framePosition")
                    return bytesWritten
                }

                // Jump verse if stuck at the end of a range
                if (framePosition == startingVerse.lastFrame() && startIndex < verses.lastIndex) {
                    seek(verses[startIndex + 1].firstFrame())
                    startIndex++
                }

                for (verseIdx in startIndex until verses.size) {
                    if (framesToRead <= 0 || framePosition !in bounds) break

                    val verse = verses[verseIdx]
                    val sectors = verse.getSectorsFromOffset(framePosition, framesToRead)

                    if (sectors.isEmpty()) {
                        // logger.info("sectors is empty for verse index ${verseIdx}")
                        continue
                    }

                    val framesTaken = sectors.sumOf { it.length() }

                    // logger.info("reading from sectors: $sectors")

                    for (sector in sectors) {
                        if (framesToRead <= 0 || framePosition !in bounds) break

                        val seekLoc = (sector.first * frameSizeInBytes).toLong()
                        if (seekLoc <= 0) {
                            // logger.error("Sector seek produced a negative seek location: $seekLoc, from ${sector}")
                        }
                        // logger.info("in sector ${sector} seeking to position $seekLoc")
                        raf.seek(seekLoc)
                        val temp = ByteArray(framesTaken * frameSizeInBytes)
                        val toCopy = raf.read(temp)
                        try {
                            System.arraycopy(temp, 0, bytes, bytesWritten, toCopy)
                        } catch (_: ArrayIndexOutOfBoundsException) {
                            println("here")
                        }
                        bytesWritten += toCopy
                        position += toCopy
                        framesToRead -= toCopy / frameSizeInBytes
                    }
                }
            }


            return bytesWritten
        }

        @Synchronized
        override fun seek(sample: Int) {
            position = when {
                sample <= startBounds -> startBounds * frameSizeInBytes
                sample >= endBounds -> endBounds - 1 * frameSizeInBytes
                else -> {
                    // logger.error("seek to $sample")
                    sample * frameSizeInBytes
                }
            }
        }

        override fun open() {
            randomAccessFile?.let { release() }
            randomAccessFile = RandomAccessFile(scratchAudio.file, "r")
            // logger.info("open called")
        }

        override fun release() {
            // logger.info("release called")
            if (randomAccessFile != null) {
                randomAccessFile?.close()
                randomAccessFile = null
            }
            //synchronized(openReaderConnections) {
            openReaderConnections.remove(this)
            //}
        }

        override fun close() {
            release()
        }
    }
}