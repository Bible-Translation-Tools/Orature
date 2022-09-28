package org.wycliffeassociates.otter.common.audio.wav

import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private data class Chunk(val label: String, val start: Int, val chunkSize: Int)

enum class ParseResult {
    VALID_NORMAL_WAV,
    VALID_EXTENDED_HEADER_WAV,
    INVALID
}

class WavHeader {

    private var readHeadPosition = 0
    private var dataChunkStart = 0

    val totalHeaderSize: Int
        get() = dataChunkStart

    internal var totalDataLength = 0
        private set
    internal var totalAudioLength = 0
        private set

    var channels = 0
        private set
    var bitsPerSample = 0
        private set
    var sampleRate = 0
        private set
    var byteRate = 0
        private set
    var blockAlign = 0
        private set

    private val chunks: MutableList<Chunk> = mutableListOf()

    fun parse(file: File): ParseResult {
        chunks.clear()
        if (file.length() < 8) {
            return ParseResult.INVALID
        }
        file.inputStream().use {
            if (!validateRiff(it)) {
                return ParseResult.INVALID
            }
            while (it.available() > 8) {
                parseChunk(it)
            }
        }

        val preDataChunk = mutableListOf<Chunk>()
        for (chunk in chunks) {
            if (chunk.label == "data") {
                dataChunkStart = chunk.start
                break
            } else {
                preDataChunk.add(chunk)
            }
        }

        computeTotalAudioSize()

        // The fmt chunk is required in all WAV files, WAVs with extensions will have additional pre-data chunks.
        preDataChunk.removeAll { it.label == "fmt " }

        if (!chunks.map { it.label }.containsAll(listOf("fmt ", "data"))) {
            return ParseResult.INVALID
        }

        readFmtChunk(file)

        if (preDataChunk.size > 0) {
            return ParseResult.VALID_EXTENDED_HEADER_WAV
        }

        return ParseResult.VALID_NORMAL_WAV
    }

    fun readFmtChunk(file: File) {
        val fmt = chunks.find { it.label == "fmt " }
        fmt?.let { fmt ->
            file.inputStream().use {
                it.skip(fmt.start.toLong())
                val fmtData = ByteArray(fmt.chunkSize)
                it.read(fmtData)
                val bb = ByteBuffer.wrap(fmtData)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                val pcm = bb.short
                channels = bb.short.toInt()
                sampleRate = bb.int
                byteRate = bb.int
                blockAlign = bb.short.toInt()
                bitsPerSample = bb.short.toInt()
            }
        }
    }

    private fun computeTotalAudioSize() {
        val data = chunks.find { it.label == "data" }
        data?.let { data ->
            totalAudioLength = data.chunkSize
        }
    }

    private fun parseChunk(inputStream: FileInputStream) {
        if (inputStream.available() < 8) {
            return
        }

        val bytes = ByteArray(4)
        inputStream.read(bytes)
        var bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val label = bb.getText(4)

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val size = bb.int

        readHeadPosition += 8

        val skip = wordAlign(size)
        inputStream.skip(skip.toLong())
        chunks.add(Chunk(label, readHeadPosition, size))

        readHeadPosition += skip
    }

    private fun validateRiff(inputStream: FileInputStream): Boolean {
        if (inputStream.available() < 12) {
            return false
        }

        val bytes = ByteArray(4)
        inputStream.read(bytes)
        var bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val label = bb.getText(4)

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val size = bb.int

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val form = bb.getText(4)

        chunks.add(Chunk("RIFF", 8, size))
        totalDataLength = size
        readHeadPosition += 12
        return label == "RIFF" && form == "WAVE"
    }
}
