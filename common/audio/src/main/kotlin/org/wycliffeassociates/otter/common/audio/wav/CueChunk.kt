/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.audio.wav

import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.wycliffeassociates.otter.common.audio.AudioCue

private const val CUE_LABEL = "cue "
private const val DATA_LABEL = "data"
private const val LIST_LABEL = "LIST"
private const val ADTL_LABEL = "adtl"
private const val LABEL_LABEL = "labl"

private const val CUE_HEADER_SIZE = 4
private const val CUE_COUNT_SIZE = 4
private const val CUE_ID_SIZE = 4
private const val CUE_DATA_SIZE = 24

// We only care to read the cue id and location, seek over the next 16 bytes
private const val DONT_CARE_CUE_DATA_SIZE = 16

/**
 * Cue Chunk
 * Our cue chunks contain a combination of two chunks:
 * 1. A Cue chunk containing cue points and their locations
 * 2. A labl chunk containing labels associated with the cue chunks
 *
 * The Cue chunk is of the following format:
 * [size - description - value (if constant)]
 * 4 - Chunk Id - " cue"
 * 4 - Chunk Data Size
 * 4 - Cue count
 * _ - List of cue points
 *
 * The List of cue points is in the following format:
 * 4 - unique cue ID
 * 4 - play order position
 * 4 - RIFF ID of corresponding data chunk - 0 (we only expect one wav chunk)
 * 4 - Byte offset of data chunk - 0 (0 is for uncompressed wav formats)
 * 4 - Block start (byte offset to sample of first channel)
 * 4 - Sample offset (byte offset to sample byte of first channel)
 *
 * Thus, each cue is 24 bytes, so the total data size of the cue chunk is:
 * 4 + (cue count) * 24
 * where 4 accounts for the field storing the number of cues
 *
 * In regards to parsing, we seek over the RIFF ID, Byte offset, Block start, sample offset
 * Play order position should be the actual location frame, so just the first two fields
 * contain information we care about.
 *
 * The labl chunk is in the following format:
 * 4 - chunk id - "list"
 * 4 - chunk data size
 * 4 - list type label - "adtl"
 *
 * For each cue, there will then be a labl entry:
 * 4 - chunk id - "labl"
 * 4 - chunk data size
 * 4 - cue point id (matching the id from the cue chunk)
 * _ - text of the label (should be word aligned, but technically we double word align
 */
internal class CueChunk : RiffChunk {

    val cues: List<AudioCue> = mutableListOf()

    val cueChunkSize: Int
        get() = CUE_HEADER_SIZE + (CUE_DATA_SIZE * cues.size)

    override val totalSize: Int
        get(): Int {
            return if (cues.isNotEmpty()) {
                val totalCueChunk = CHUNK_HEADER_SIZE + cueChunkSize
                // adds 1 to cue size for the extra chunk header and label
                // that the list chunk adds as overhead
                val totalLabelChunk =
                    ((CHUNK_LABEL_SIZE + CHUNK_HEADER_SIZE) * (cues.size + 1)) + computeTextSize(cues)
                return totalCueChunk + totalLabelChunk
            } else 0
        }

    fun addCue(cue: AudioCue) {
        cues as MutableList
        cues.add(cue)
    }

    fun addCues(cues: List<AudioCue>) {
        cues as MutableList
        cues.addAll(cues)
    }

    override fun toByteArray(): ByteArray {
        if (cues.isEmpty()) {
            return ByteArray(0)
        }

        cues as MutableList
        cues.sortBy { it.location }
        val cueChunkBuffer = ByteBuffer.allocate(CHUNK_HEADER_SIZE + cueChunkSize)
        cueChunkBuffer.order(ByteOrder.LITTLE_ENDIAN)
        cueChunkBuffer.put(CUE_LABEL.toByteArray(Charsets.US_ASCII))
        cueChunkBuffer.putInt(CUE_DATA_SIZE * cues.size + CUE_COUNT_SIZE)
        cueChunkBuffer.putInt(cues.size)
        for (i in cues.indices) {
            cueChunkBuffer.put(createCueData(i, cues[i]))
        }
        val labelChunkArray = createLabelChunk(cues)
        val combinedBuffer = ByteBuffer.allocate(cueChunkBuffer.capacity() + labelChunkArray.size)
        combinedBuffer.put(cueChunkBuffer.array())
        combinedBuffer.put(labelChunkArray)
        return combinedBuffer.array()
    }

    private fun createCueData(cueNumber: Int, cue: AudioCue): ByteArray {
        val buffer = ByteBuffer.allocate(CUE_DATA_SIZE)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(cueNumber)
        buffer.putInt(cue.location)
        buffer.put(DATA_LABEL.toByteArray(Charsets.US_ASCII))
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(cue.location)
        return buffer.array()
    }

    private fun createLabelChunk(cues: List<AudioCue>): ByteArray {
        // size = (8 for labl header, 4 for cue id) * num cues + all strings
        val size = (CHUNK_HEADER_SIZE + CHUNK_LABEL_SIZE) * cues.size + computeTextSize(cues)
        // adds LIST header which is a standard chunk header and a "adtl" label
        val buffer = ByteBuffer.allocate(size + CHUNK_HEADER_SIZE + CHUNK_LABEL_SIZE)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put(LIST_LABEL.toByteArray(Charsets.US_ASCII))
        buffer.putInt(size + CHUNK_LABEL_SIZE)
        buffer.put(ADTL_LABEL.toByteArray(Charsets.US_ASCII))
        for (i in cues.indices) {
            buffer.put(LABEL_LABEL.toByteArray(Charsets.US_ASCII))
            val label = wordAlignedLabel(cues[i])
            buffer.putInt(CUE_ID_SIZE + label.size) // subchunk size here is label size plus id
            buffer.putInt(i)
            buffer.put(label)
        }
        return buffer.array()
    }

    private fun computeTextSize(cues: List<AudioCue>): Int {
        return cues
            .map { getWordAlignedLength(it.label.length) }
            .sum()
    }

    private fun getWordAlignedLength(length: Int) =
        if (length % DWORD_SIZE != 0) length + DWORD_SIZE - (length % DWORD_SIZE) else length

    private fun wordAlignedLabel(cue: AudioCue): ByteArray {
        val label = cue.label
        val alignedLength = getWordAlignedLength(cue.label.length)
        return label.toByteArray().copyOf(alignedLength)
    }

    override fun parse(chunk: ByteBuffer) {
        val cueListBuilder = CueListBuilder()

        chunk.order(ByteOrder.LITTLE_ENDIAN)
        cueListBuilder.clear()
        while (chunk.remaining() > CHUNK_HEADER_SIZE) {
            val subchunkLabel = chunk.getText(CHUNK_LABEL_SIZE)
            val subchunkSize = chunk.int

            if (chunk.remaining() < subchunkSize) {
                throw InvalidWavFileException(
                    """Chunk $subchunkLabel is of size: $subchunkSize 
                        |but remaining chunk size is ${chunk.remaining()}""".trimMargin()
                )
            }

            // section off a buffer from this one that can be used for parsing the nested chunk
            val buffer = chunk.slice()
            buffer.limit(buffer.position() + subchunkSize)

            when (subchunkLabel) {
                LIST_LABEL -> parseLabels(buffer, cueListBuilder)
                CUE_LABEL -> parseCue(buffer, cueListBuilder)
                else -> Unit
            }

            // move on to the next chunk
            chunk.seek(subchunkSize)
        }
        cues as MutableList
        cues.clear()
        cues.addAll(cueListBuilder.build())
    }

    /**
     * Parses a cue chunk, which adhere to the following format:
     * "cue " (0x6375 6520)
     * size of cue chunk (4B LE)
     * number of cues (4B LE)
     * For each cue:
     * cue ID (4B LE)
     * location (index in PCM array considering 44100 indicies per second [so don't consider block size])
     * "data" (0x6461 7461)
     * 0000 0000
     * 0000 0000
     * location (again, same as above)
     *
     *
     * NOTE This method assumes that the first 8 bytes have already been removed and parsed.
     *
     * @param chunk
     */
    private fun parseCue(chunk: ByteBuffer, cueListBuilder: CueListBuilder) {
        chunk.order(ByteOrder.LITTLE_ENDIAN)
        if (!chunk.hasRemaining()) {
            return
        }
        // read number of cues
        val numCues = chunk.int

        // each cue subchunk should be 24 bytes, plus 4 for the number of cues field
        if (chunk.remaining() != CUE_DATA_SIZE * numCues) {
            throw InvalidWavFileException()
        }

        // For each cue, read the cue Id and the cue location
        for (i in 0 until numCues) {
            val cueId = chunk.int
            val cueLoc = chunk.int

            cueListBuilder.addLocation(cueId, cueLoc)

            // Skip the next 16 bytes to the next cue point
            chunk.seek(DONT_CARE_CUE_DATA_SIZE)
        }
    }

    private fun parseLabels(chunk: ByteBuffer, cueListBuilder: CueListBuilder) {
        chunk.order(ByteOrder.LITTLE_ENDIAN)

        // Skip List Chunks that are not subtype "adtl"
        if (chunk.remaining() < CHUNK_LABEL_SIZE || ADTL_LABEL != chunk.getText(CHUNK_LABEL_SIZE)) {
            return
        }

        while (chunk.remaining() > CHUNK_HEADER_SIZE) {
            val subchunk = chunk.getText(CHUNK_LABEL_SIZE)
            val subchunkSize = chunk.int

            // chunk data must be word aligned but the size might not account for padding
            // therefore, if odd, the size we read must add one to include the padding
            // https://sharkysoft.com/jwave/docs/javadocs/lava/riff/wave/doc-files/riffwave-frameset.htm
            val wordAlignedSubchunkSize = subchunkSize + if (subchunkSize % 2 == 0) 0 else 1

            when (subchunk) {
                LABEL_LABEL -> {
                    val id = chunk.int
                    val labelBytes = ByteArray(wordAlignedSubchunkSize - CHUNK_LABEL_SIZE)
                    chunk.get(labelBytes)
                    // trim necessary to strip trailing 0's used to pad to double word align
                    val label = String(labelBytes, Charsets.US_ASCII).trim { it.toByte() == 0.toByte() }
                    cueListBuilder.addLabel(id, label)
                }
                else -> {
                    chunk.seek(wordAlignedSubchunkSize)
                }
            }
        }
    }
}

private class CueListBuilder {

    private data class TempCue(var location: Int?, var label: String?)

    private val map = mutableMapOf<Int, TempCue>()

    fun addLocation(id: Int, location: Int?) {
        map[id]?.let {
            it.location = location
        } ?: map.put(id, TempCue(location, null))
    }

    fun addLabel(id: Int, label: String) {
        map[id]?.let {
            it.label = label
        } ?: map.put(id, TempCue(null, label))
    }

    fun build(): List<AudioCue> {
        return map.values.mapNotNull { cue ->
            cue.location?.let { loc ->
                cue.label?.let { label ->
                    AudioCue(loc, label)
                }
            }
        }
    }

    fun clear() {
        map.clear()
    }
}
