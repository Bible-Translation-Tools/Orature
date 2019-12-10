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

    private val cues = mutableListOf<WavCue>()
    private val cueListBuilder = CueListBuilder()

    val cueChunkSize: Int
        get() = CUE_HEADER_SIZE + (CUE_DATA_SIZE * cues.size)

    fun addCue(cue: WavCue) {
        cues.add(cue)
    }

    internal fun writeChunk(output: OutputStream): Int {
        cues.sortBy { it.location }
        val bytes = ByteArray(8 + cueChunkSize)
        val buffer = ByteBuffer.wrap(bytes)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        for(i in cues.indices){
            buffer.put(createCueChunk(i, cues[i]))
        }
        buffer.put(createLabelChunk(cues))
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
        val size = cues.size * 40 + 4 + computeTextSize()
        val buffer = ByteBuffer.allocate(size + 8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("LIST".toByteArray(Charsets.US_ASCII))
        buffer.putInt(size)
        buffer.put("adtl".toByteArray(Charsets.US_ASCII))
        for (i in cues.indices) {
            buffer.put("ltxt".toByteArray(Charsets.US_ASCII))
            buffer.putInt(20)
            buffer.putInt(i)
            buffer.putInt(0)
            buffer.put("rvn ".toByteArray(Charsets.US_ASCII))
            buffer.putInt(0)
            buffer.putInt(0)
            buffer.put("labl".toByteArray(Charsets.US_ASCII))
            val label = wordAlignedLabel(cues[i])
            buffer.putInt(4 + label.size)
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
        while (chunk.hasRemaining()) {
            if ("ltxt" == chunk.getText(LABEL_SIZE)) {
                var size = chunk.int
                //move to skip ltxt subchunk
                chunk.seek(size)
                if ("labl" == chunk.getText(LABEL_SIZE)) {
                    size = chunk.int
                    val id = chunk.int
                    val labelBytes = ByteArray(size - 4)
                    chunk.get(labelBytes)
                    //trim necessary to strip trailing 0's used to pad to word align
                    val label = String(labelBytes, Charsets.US_ASCII).trim { it <= ' ' }
                    cueMap[id]?.let {
                        it.label = label
                    } ?: cueMap.put(id, CueBuilder(-1, label))
                } else {
                    //else skip over this subchunk
                    size = chunk.int
                    chunk.seek(size)
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

    fun build(): List<WavCue> {
        val cues = mutableListOf<WavCue>()
        for(cue in map.values) {
            cue.location?.let { loc ->
                cue.label?.let {label ->
                    cues.add(WavCue(loc, label))
                }
            }
        }
        return cues
    }
}