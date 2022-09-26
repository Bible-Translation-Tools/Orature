package org.wycliffeassociates.otter.common.audio.wav

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.Exception
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
    private var totalAudioSize = 0

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
            if (chunk.label == "DATA") {
                dataChunkStart = chunk.start
                break
            } else {
                preDataChunk.add(chunk)
            }
        }

        computeTotalAudioSize()

        // The fmt chunk is required in all WAV files, WAVs with extensions will have additional pre-data chunks.
        preDataChunk.removeAll { it.label == "fmt " }

        if (!chunks.map { it.label }.containsAll(listOf("fmt ", "DATA"))) {
            return ParseResult.INVALID
        }

        if (preDataChunk.size > 0) {
            return ParseResult.VALID_EXTENDED_HEADER_WAV
        }

        return ParseResult.VALID_NORMAL_WAV
    }

    private fun computeTotalAudioSize() {
        val data = chunks.find { it.label == "DATA" }
        data?.let { data ->
            totalAudioSize = data.chunkSize
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

        readHeadPosition += 12
        return label == "RIFF" && form == "WAVE"
    }
}
