/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.reactivex.rxkotlin.toObservable
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.BOOK_TITLE_SORT
import org.wycliffeassociates.otter.common.data.primitives.CHAPTER_TITLE_SORT
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.device.AudioFileReaderProvider
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.atomic.AtomicInteger
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

    val frameSizeInBytes: Int
        get() = channels * (scratchAudio.bitsPerSample / 8)

    private val sampleRate: Int
        get() = scratchAudio.sampleRate

    private val channels: Int
        get() = scratchAudio.channels

    private val sampleSize: Int
        get() = scratchAudio.bitsPerSample

    @get:Synchronized
    val totalFrames: Int
        get() = activeVerses.sumOf { indexToFrame(it.length) }

    @get:Synchronized
    internal val activeVerses: List<VerseNode>
        get() = totalVerses.filter { it.placed }

    @Synchronized
    fun getActiveMarkers(): List<AudioMarker> {
        val verses = activeVerses
            .map {
                it.copyMarker(
                    frame = absoluteFrameToRelativeChapterFrame(indexToFrame(it.firstIndex()))
                )
            }
        return verses
    }

    internal val totalVerses: MutableList<VerseNode>

    private lateinit var serializedVersesFile: File
    private val activeVersesMapper = ObjectMapper()
        .registerKotlinModule()
        .apply {
            this.registerSubtypes(
                NamedType(VerseMarker::class.java, "VerseMarker"),
                NamedType(ChapterMarker::class.java, "ChapterMarker"),
                NamedType(BookMarker::class.java, "BookMarker")
            )
        }

    val onActiveVersesUpdated = PublishSubject.create<List<AudioMarker>>()

    // Represents an ever growing tape of audio. This tape may have "dirty" sectors corresponding to outdated
    // content, which needs to be removed before finalizing the audio.
    lateinit var scratchAudio: OratureAudioFile
        private set

    init {
        totalVerses = initializeActiveVerses()
        initializeWorkingAudioFile()
        initializeSerializedVersesFile()
    }

    private fun initializeActiveVerses(): MutableList<VerseNode> {
        return chapter
            .chunks
            .take(1)
            .map { chunks ->
                chunks.map { chunk ->
                    when (chunk.sort) {
                        BOOK_TITLE_SORT -> BookMarker(workbook.source.slug, 0)
                        CHAPTER_TITLE_SORT -> ChapterMarker(chapter.sort, 0)
                        else -> VerseMarker(chunk.start, chunk.end, 0)
                    }
                }
            }
            .flatMap { it.toObservable() }
            .map { marker ->
                VerseNode(false, marker)
            }
            .toList()
            .blockingGet()
    }

    fun loadFromSerializedVerses() {
        val json = serializedVersesFile.readText()
        if (json.isEmpty()) {
            return
        }
        val reference = object : TypeReference<List<VerseNode>>() {}

        try {
            val activeNodes = activeVersesMapper.readValue(json, reference)
            logger.info("Loading ${activeNodes.size} audio markers from serialized data")
            totalVerses.forEach {
                it.clear() // reset the node's sectors
            }
            activeNodes.forEach { node ->
                // find node position in totalVerses
                val index = totalVerses.indexOfFirst {
                    it.marker.formattedLabel == node.marker.formattedLabel
                }
                if (index != -1) {
                    totalVerses[index] = node
                }
            }
        } catch (e: JsonMappingException) {
            logger.error("Error in loadFromSerializedVerses: ${e.message}")
        }
    }

    fun finalizeVerse(verseIndex: Int, history: NarrationHistory? = null): Int {
        val endIndex = frameToIndex(scratchAudio.totalFrames)

        history?.finalizeVerse(endIndex, totalVerses)
        onVersesUpdated()
        return endIndex - 1 // subtract 1 due to the sector being finalized with `until`
    }

    fun onVersesUpdated() {
        updateTotalVerses()
        serializeVerses()
        publishActiveVerses()
    }

    private fun updateTotalVerses() {
        activeVerses.forEach { verseNode ->
            val newFrame = absoluteFrameToRelativeChapterFrame(indexToFrame(verseNode.firstIndex()))
            val updatedMarker = verseNode.copyMarker(frame = newFrame)

            // find node position in totalVerses
            val index = totalVerses.indexOfFirst {
                it.marker.formattedLabel == verseNode.marker.formattedLabel
            }
            if (index != -1) {
                totalVerses[index] = VerseNode(
                    true, updatedMarker, totalVerses[index].sectors
                )
            }
        }
    }

    private fun serializeVerses() {
        val jsonStr = activeVersesMapper.writeValueAsString(activeVerses)
        serializedVersesFile.writeText(jsonStr)
    }

    private fun publishActiveVerses() {
        val updatedVerses = if (activeVerses.isNotEmpty()) {
            activeVerses.map {
                val newFrame = absoluteFrameToRelativeChapterFrame(indexToFrame(it.firstIndex()))
                it.copyMarker(frame = newFrame)
            }
        } else listOf()

        onActiveVersesUpdated.onNext(updatedVerses)
    }

    /**
     * Remove old audio data from chapter representation file and update serialized verses file
     */
    fun trim() {
        logger.info("Trimming chapter representation file")
        trimScratchAudio()
        trimActiveVerses()
    }

    private fun trimScratchAudio() {
        val chapterDir = workbook.projectFilesAccessor.getChapterAudioDir(workbook, chapter)
        val newScratchAudio = chapterDir.resolve("new_$CHAPTER_NARRATION_FILE_NAME")

        newScratchAudio.outputStream().use { writer ->
            getAudioFileReader().use { reader ->
                reader.open()
                reader.seek(0)
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (reader.hasRemaining()) {
                    val read = reader.getPcmBuffer(buffer)
                    writer.write(buffer, 0, read)
                }
            }
        }

        scratchAudio.file.delete()
        newScratchAudio.renameTo(scratchAudio.file)
    }

    private fun trimActiveVerses() {
        var start = 0
        activeVerses.forEach { verse ->
            val sectors = mutableListOf<IntRange>()

            val end = start + verse.length - 1
            sectors.add(IntRange(start, end))
            start = end + 1

            verse.sectors.clear()
            verse.sectors.addAll(sectors)
        }

        serializeVerses()
    }

    fun versesWithRecordings(): List<Boolean> {
        return totalVerses.map { it.placed && it.length > 0 }
    }

    fun getCompletionProgress(): Double {
        val totalVerses = totalVerses.size
        val activeVerses = activeVerses.size

        return if (totalVerses > 0) {
            activeVerses / totalVerses.toDouble()
        } else 0.0
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
     * audio only contained the segments referenced by the active verse nodes.
     */
    internal fun absoluteFrameToRelativeChapterFrame(absoluteFrame: Int): Int {
        val verses = activeVerses
        val verse = findVerse(absoluteFrame)
        verse?.let {
            val index = verses.indexOf(verse)
            var rel = 0
            for (idx in 0 until index) {
                rel += indexToFrame(verses[idx].length)
            }
            rel += indexToFrame(it.indicesToPosition(frameToIndex(absoluteFrame)))
            return rel
        }
        return 0
    }

    fun findVerse(absoluteFrame: Int): VerseNode? {
        val absoluteIndex = frameToIndex(absoluteFrame)
        return activeVerses.find { node ->
            absoluteIndex in node
        }
    }

    /**
     * Converts a relative frame (audio only taking into account the currently active verses)
     * to an absolute position into the scratch audio file. This conversion is performed by counting frames through
     * the range of each active verse.
     */
    internal fun relativeChapterFrameToAbsoluteIndex(relativeFrame: Int): Int {
        val relativeIdx = frameToIndex(relativeFrame)
        var remaining = relativeIdx + 1
        val verses = activeVerses
        if (relativeIdx <= 0 && activeVerses.isEmpty()) {
            return if (scratchAudio.totalFrames == 0) 0 else (frameToIndex(scratchAudio.totalFrames)) + 1
        }
        if (relativeIdx <= 0) return activeVerses.first().firstIndex()

        for (verse in verses) {
            for (sector in verse.sectors) {
                if (sector.length() < remaining) {
                    remaining -= sector.length()
                } else if (sector.length() == remaining) {
                    return sector.last
                } else {
                    return sector.first + remaining - 1
                }
            }
        }

        // logger.error("RelativeToAbsolute did not resolve before iterating over active verses. Relative index: ${relativeIdx}")
        return if (verses.isNotEmpty()) verses.last().lastIndex() else frameToIndex(scratchAudio.totalFrames)
    }

    /**
     * Gets the range of frames of an audio marker
     *
     * @return the range of frames of an audio marker
     */
    fun getRangeOfMarker(verse: AudioMarker): IntRange? {
        val verses = activeVerses.map { it }
        if (verses.isEmpty()) return null

        verses
            .find { it.marker.label == verse.label }
            ?.let { _verse ->
                return indexToFrame(_verse.firstIndex()) .. indexToFrame(_verse.lastIndex())
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
                    position = node.firstIndex()
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
            position = verses.get(index).firstIndex()
        }

        @get:Synchronized
        val absoluteFramePosition: Int
            get() = indexToFrame(position)

        override val framePosition: Int
            get() = absoluteFrameToRelativeFrame(absoluteFramePosition)


        /**
         * Converts an absoluteFrame position in the scratch audio file to a position relative to the
         * "relative verse space" or "relative chapter space", depending on the value stored in lockToVerse.
         * When lockToVerse is not CHAPTER_UNLOCKED, the absolute frame is mapped to a position relative to the verse specified by
         * lockToVerse. When lockToVerse is equal to CHAPTER_UNLOCKED, the absoluteFrame position is mapped to a position relative to the
         * chapter.
         */
        internal fun absoluteFrameToRelativeFrame(absoluteFrame: Int): Int {
            val lockedVerse = lockToVerse.get()
            return if (lockedVerse == CHAPTER_UNLOCKED) {
                absoluteFrameToRelativeChapterFrame(absoluteFrame)
            } else {
                absoluteFrameToRelativeVerseFrame(absoluteFrame, lockedVerse)
            }
        }

        /**
         * Converts an absoluteFrame position in the scratch audio file to a position in the "relative verse space".
         * This is performed by finding the verse that contains the absolute frame, and counting how many frames are
         * from the start of the verse, to the given absoluteFrame position.
         */
        internal fun absoluteFrameToRelativeVerseFrame(absoluteFrame: Int, verseIndex: Int): Int {
            val verse = activeVerses.getOrNull(verseIndex)
            var rel = 0
            verse?.let {
                rel = indexToFrame(it.indicesToPosition(frameToIndex(absoluteFrame)))
            }
            return rel
        }


        @get:Synchronized
        override val totalFrames: Int
            get() {
                val lockedVerse = lockToVerse.get()
                return if (lockedVerse == CHAPTER_UNLOCKED) {
                    this@ChapterRepresentation.totalFrames
                } else {
                    indexToFrame(activeVerses[lockedVerse].length)
                }
            }

        @Synchronized
        override fun hasRemaining(): Boolean {
            if (totalFrames == 0) return false
            if (randomAccessFile == null) return false

            val currentIndex = position
            val verses = activeVerses
            val verseIndex = lockToVerse.get()
            val hasRemaining = if (verseIndex != CHAPTER_UNLOCKED) {
                currentIndex in verses[verseIndex] && currentIndex != verses[verseIndex].lastIndex()
            } else {
                verses.any { currentIndex in it } && currentIndex != verses.last().lastIndex()
            }
            return hasRemaining
        }

        override fun supportsTimeShifting(): Boolean {
            return false
        }

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
            var currentVerseIndex = verses.indexOfFirst { position in it }
            if (currentVerseIndex == -1) {
                currentVerseIndex = 0
            }

            val verse = verses.getOrNull(currentVerseIndex)
            return verse
        }

        @Synchronized
        private fun getPcmBufferVerse(bytes: ByteArray, verse: VerseNode): Int {
            var bytesWritten = 0
            randomAccessFile?.let { raf ->
                if (position !in verse) {
                    return 0
                }

                var bytesToRead = min(bytes.size, verse.length)

                // if there is a negative frames to read or
                if (bytesToRead <= 0 || position !in verse) {
                    logger.error("Frames to read is negative: $position, $bytesToRead, ${verse.marker.formattedLabel}")
                    position = verse.lastIndex()
                    return 0
                }

                val sectors = verse.getSectorsFromOffset(position, bytesToRead)

                if (sectors.isEmpty()) {
                    logger.error("sectors is empty for verse ${verse.marker.label}")
                    position = verse.lastIndex()
                    return 0
                }

                for (sector in sectors) {
                    if (sector.length() == 0) continue
                    if (bytesToRead <= 0 || position !in verse) break

                    val bytesToCopyFromSector = max(min(bytesToRead, sector.length()), 0)

                    val seekLoc = (sector.first).toLong()
                    raf.seek(seekLoc)
                    val temp = ByteArray(bytesToCopyFromSector)
                    val toCopy = raf.read(temp)
                    try {
                        System.arraycopy(temp, 0, bytes, bytesWritten, toCopy)

                        bytesWritten += toCopy
                        position += toCopy
                        bytesToRead -= toCopy
                    } catch (e: ArrayIndexOutOfBoundsException) {
                        logger.error("arraycopy out of bounds, $bytesWritten bytes written, $toCopy toCopy", e)
                    }

                    // this would move the reader back to a read value
                    // because the next unread value might NOT be the next position! need to look at the next sector
                    if (position !in sector) {
                        position = sector.last
                    }
                }

                if (position == verse.lastIndex()) {
                    adjustPositionToNextVerse(verse)
                } else {
                    adjustPositionToNextSector(verse)
                }

                return bytesWritten
            } ?: throw IllegalAccessException("getPcmBufferVerse called before opening file")
        }

        private fun adjustPositionToNextSector(verse: VerseNode) {
            if (position == verse.lastIndex()) return
            if (position !in verse) return

            val sectors = verse.sectors
            val sectorIndex = sectors.indexOfFirst { position in it }!!

            if (position == sectors[sectorIndex].last) {
                position = sectors[sectorIndex + 1].first
            } else position += 1
        }

        private fun adjustPositionToNextVerse(verse: VerseNode) {
            if (lockToVerse.get() != CHAPTER_UNLOCKED) return

            val verses = activeVerses
            val index = verses.indexOf(verse)
            if (index == verses.lastIndex) return

            position = verses[index + 1].firstIndex()
        }

        fun frameInVerseToFrameInChapter(frame: Int, verseIndex: Int): Int {
            val verse = activeVerses[verseIndex]
            return frame + absoluteFrameToRelativeChapterFrame(indexToFrame(verse.firstIndex()))
        }

        @Synchronized
        override fun seek(frame: Int) {
            // If we are locked to a verse, we assume that the frame is in the relative verse space,
            // so we need to map the frame to call relativeVerseToRelativeChapter(), then pass the return to
            // relativeToAbsolute
            val lockedVerse = lockToVerse.get()
            val relativeChapterFrame = if (lockedVerse != CHAPTER_UNLOCKED) {
                frameInVerseToFrameInChapter(frame, lockedVerse)
            } else {
                frame
            }
            position = relativeChapterFrameToAbsoluteIndex(relativeChapterFrame)
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
            openReaderConnections.remove(this)
        }

        override fun close() {
            release()
        }
    }

    private fun frameToIndex(frame: Int) = frame * frameSizeInBytes
    private fun indexToFrame(index: Int) = index / frameSizeInBytes
}