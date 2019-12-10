package org.wycliffeassociates.otter.jvm.markerapp.audio.wav

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


private const val CUE_LABEL = "cue "
private const val DATA_LABEL = "data"

private const val LABEL_SIZE = 4
private const val CUE_HEADER_SIZE = 4
private const val CUE_DATA_SIZE = 24

class CueChunk(val wavFile: WavFile) {

    private var cues = mutableListOf<WavCue>()
    private val cueListBuilder = CueListBuilder()

    val cueChunkSize: Int
        get() = CUE_HEADER_SIZE + (CUE_DATA_SIZE * cues.size)

    fun addCue(cue: WavCue) {
        cues.add(cue)
    }

    fun writeChunk(output: OutputStream): ByteBuffer {
        cues.sortBy { it.location }
        val buffer = ByteBuffer.allocate(8 + cueChunkSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        for (i in cues.indices) {
            buffer.put(createCueChunk(i, cues[i]))
        }
        buffer.put(createLabelChunk(cues))
        return buffer
    }

    fun createCueChunk(cueNumber: Int, cue: WavCue): ByteArray {
        val buffer = ByteBuffer.allocate(CUE_DATA_SIZE)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(cueNumber)
        buffer.putInt(cue.location)
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(0)
        buffer.putInt(0)
        buffer.putInt(cue.location)
        return buffer.array()
    }

    fun createLabelChunk(cues: List<WavCue>): ByteArray {
        val size = 12 * cues.size + computeTextSize(cues) // all strings + (8 for labl header, 4 for cue id) * num cues
        val buffer = ByteBuffer.allocate(size + 8) // adds LIST header
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("LIST".toByteArray(Charsets.US_ASCII))
        buffer.putInt(size)
        buffer.put("adtl".toByteArray(Charsets.US_ASCII))
        for (i in cues.indices) {
            buffer.put("labl".toByteArray(Charsets.US_ASCII))
            val label = wordAlignedLabel(cues[i])
            buffer.putInt(4 + label.size) // subchunk size here is label size plus id
            buffer.putInt(i)
            buffer.put(label)
        }
        return buffer.array()
    }

    private fun computeTextSize(cues: List<WavCue>): Int {
        var total = 0
        for (i in cues.indices) {
            val length = cues[i].label.length
            total += getWordAlignedLength(length)
        }
        return total
    }

    private fun getWordAlignedLength(length: Int): Int {
        var length = length
        if (length % 4 != 0) {
            length += 4 - length % 4
        }
        return length
    }

    private fun wordAlignedLabel(cue: WavCue): ByteArray {
        val label = cue.label
        var alignedLength = cue.label.length
        if (alignedLength % 4 != 0) {
            alignedLength += 4 - alignedLength % 4
        }
        return label.toByteArray().copyOf(alignedLength)
    }


    fun parseMetadata(chunk: ByteBuffer) {
        cueListBuilder.clear()
        while (chunk.remaining() > 8) {
            val subchunkLabel = chunk.getText(LABEL_SIZE)
            val subchunkSize = chunk.int

            if (chunk.remaining() < subchunkSize) {
                throw InvalidWavFileException(
                    "Chunk $subchunkLabel is of size: $subchunkSize but remaining chunk size is ${chunk.remaining()}"
                )
            }

            // section off a buffer from this one that can be used for parsing the nested chunk
            val buffer = chunk.slice()
            buffer.limit(buffer.position() + subchunkSize)

            when (subchunkLabel) {
                "LIST" -> parseLabels(buffer)
                "cue " -> parseCue(buffer)
                else -> {
                }
            }

            // move on to the next chunk
            chunk.seek(subchunkSize)
        }
        cues = cueListBuilder.build()
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
    private fun parseCue(chunk: ByteBuffer) {
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
            chunk.seek(16)
        }
    }


    private fun parseLabels(chunk: ByteBuffer) {
        while (chunk.remaining() > 8) {
            val subchunk = chunk.getText(LABEL_SIZE)
            val subchunkSize = chunk.int
            when (subchunk) {
                "labl" -> {
                    val id = chunk.int
                    val labelBytes = ByteArray(subchunkSize - 4)
                    chunk.get(labelBytes)
                    //trim necessary to strip trailing 0's used to pad to word align
                    val label = String(labelBytes, Charsets.US_ASCII).trim()
                    cueListBuilder.addLabel(id, label)
                }
                else -> {
                    chunk.seek(subchunkSize)
                }
            }
        }
    }
}

private class CueListBuilder() {

    private data class TempCue(var location: Int?, var label: String?)

    val map = mutableMapOf<Int, TempCue>()

    fun addLocation(id: Int, location: Int?) {
        map[id]?.let {
            it.location = location
        } ?: run {
            map.put(id, TempCue(location, null))
        }
    }

    fun addLabel(id: Int, label: String) {
        map[id]?.let {
            it.label = label
        } ?: run {
            map.put(id, TempCue(null, label))
        }
    }

    fun build(): MutableList<WavCue> {
        val cues = mutableListOf<WavCue>()
        for (cue in map.values) {
            cue.location?.let { loc ->
                cue.label?.let { label ->
                    cues.add(WavCue(loc, label))
                }
            }
        }
        return cues
    }

    fun clear() {
        map.clear()
    }
}