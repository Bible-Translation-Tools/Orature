package org.wycliffeassociates.otter.common.audio.wav

import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

private data class Chunk(val label: String, val start: Int, val chunkSize: Int)

enum class WavHeaderParseResult {
    VALID_NORMAL_WAV,
    VALID_EXTENDED_HEADER_WAV,
}

private const val RIFF = "RIFF"
private const val WAVE = "WAVE"
private const val FMT = "fmt "
private const val DATA = "data"
private const val PCM: Short = 1
private const val DEFAULT_HEADER_SIZE = 44
private const val BITS_IN_BYTE = 8

class WavHeader {
    private var readHeadPosition = 0
    private var dataChunkStart = DEFAULT_HEADER_SIZE

    val totalHeaderSize: Int
        get() = dataChunkStart

    internal var totalDataLength = 0
    internal var totalAudioLength = 0

    var channels = DEFAULT_CHANNELS
        private set
    var bitsPerSample = DEFAULT_BITS_PER_SAMPLE
        private set
    var sampleRate = DEFAULT_SAMPLE_RATE
        private set
    var byteRate = sampleRate * channels * (bitsPerSample / BITS_IN_BYTE)
        private set
    var blockAlign = channels * (bitsPerSample / BITS_IN_BYTE)
        private set

    private val chunks: MutableList<Chunk> = mutableListOf()

    /**
     * Reads the header of a wav file. If additional header chunks are present, it returns a parse result indicating
     * the wav file is an extension wav file, otherwise it returns that it is a normal wav file.
     *
     * Fmt and data chunks are the two required chunks; and the "header" makes up chunks prior to the data chunk.
     */
    @Throws(InvalidWavFileException::class)
    fun parse(file: File): WavHeaderParseResult {
        chunks.clear()
        if (file.length() < CHUNK_HEADER_SIZE) {
            throw InvalidWavFileException("File length is less than a chunk header.")
        }
        file.inputStream().use {
            if (!validateRiff(it)) {
                throw InvalidWavFileException("File does not contain a RIFF header.")
            }
            while (it.available() >= CHUNK_HEADER_SIZE) {
                parseChunk(it)
            }
        }

        val preDataChunk = mutableListOf<Chunk>()
        for (chunk in chunks) {
            if (chunk.label == DATA) {
                dataChunkStart = chunk.start
                break
            } else {
                preDataChunk.add(chunk)
            }
        }

        computeTotalAudioSize()

        // The fmt chunk is required in all WAV files, WAVs with extensions will have additional pre-data chunks.
        preDataChunk.removeAll { it.label == FMT || it.label == RIFF }

        val chunkLabels: List<String> = chunks.map { it.label }
        if (!chunkLabels.containsAll(listOf(FMT, DATA))) {
            when {
                chunkLabels.containsAll(listOf(DATA)) -> {
                    throw InvalidWavFileException("Wav header missing fmt chunk.")
                }
                chunkLabels.containsAll(listOf(FMT)) -> {
                    throw InvalidWavFileException("Wav header missing data chunk.")
                }
                else -> throw InvalidWavFileException("Wav header missing both fmt and data chunks.")
            }
        }

        readFmtChunk(file)

        if (preDataChunk.size > 0) {
            return WavHeaderParseResult.VALID_EXTENDED_HEADER_WAV
        }

        return WavHeaderParseResult.VALID_NORMAL_WAV
    }

    /**
     * Reads the fmt chunk of the wav file and updates the member fields related to the audio format such as
     * sample rate, bit rate, and number of channels.
     */
    private fun readFmtChunk(file: File) {
        val fmt = chunks.find { it.label == FMT }
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

    /**
     * Retrieves the audio size which is the size of the data chunk
     */
    private fun computeTotalAudioSize() {
        val data = chunks.find { it.label == DATA }
        data?.let { data ->
            totalAudioLength = data.chunkSize
        }
    }

    /**
     * Parses an arbitrary chunk of the wav file. All chunks contain an 8 byte header containing a 4 character chunk
     * label, and a 4 byte chunk size. This method will populate a complete list of all chunks and their corresponding
     * sizes contained within the audio file. While this class is concerned primarily with the header, this list will
     * contain all chunks, including metadata chunks.
     *
     * Note that the size of a chunk does not contain a pad byte. If the size is odd, the chunk should contain a pad byte
     * at the end to word align the chunk as per the wav file spec:
     * https://www.mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/Docs/riffmci.pdf
     *
     * We accomplish this by calling the wordAlign function prior to seeking in the buffer.
     */
    private fun parseChunk(inputStream: FileInputStream) {
        if (inputStream.available() < CHUNK_HEADER_SIZE) {
            return
        }

        val bytes = ByteArray(CHUNK_LABEL_SIZE)
        inputStream.read(bytes)
        var bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val label = bb.getText(CHUNK_LABEL_SIZE)

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val size = bb.int

        readHeadPosition += CHUNK_HEADER_SIZE

        val skip = wordAlign(size)
        inputStream.skip(skip.toLong())
        chunks.add(Chunk(label, readHeadPosition, size))

        readHeadPosition += skip
    }

    /**
     * Validates that the wav file begins with RIFF, the size of the file, and that the type of RIFF file is WAVE
     */
    private fun validateRiff(inputStream: FileInputStream): Boolean {
        if (inputStream.available() < RIFF_HEADER_SIZE) {
            return false
        }

        val bytes = ByteArray(CHUNK_LABEL_SIZE)
        inputStream.read(bytes)
        var bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val label = bb.getText(CHUNK_LABEL_SIZE)

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val size = bb.int

        inputStream.read(bytes)
        bb = ByteBuffer.wrap(bytes)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val form = bb.getText(CHUNK_LABEL_SIZE)

        chunks.add(Chunk(RIFF, CHUNK_HEADER_SIZE, size))
        totalDataLength = size
        readHeadPosition += RIFF_HEADER_SIZE
        return label == RIFF && form == WAVE
    }

    /**
     * Generates a normal wav file with only the fmt and data subchunks
     *
     * see http://soundfile.sapp.org/doc/WaveFormat/ for equations
     */
    internal fun generateHeaderArray(): ByteArray {
        val header = ByteBuffer.allocate(DEFAULT_HEADER_SIZE)
        val longSampleRate = sampleRate
        val byteRate = (bitsPerSample * sampleRate * channels) / BITS_IN_BYTE

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.put(RIFF.toByteArray(Charsets.US_ASCII))
        header.putInt(totalDataLength)
        header.put(WAVE.toByteArray(Charsets.US_ASCII))
        header.put(FMT.toByteArray(Charsets.US_ASCII))
        header.putInt(bitsPerSample)
        header.putShort(PCM) // format = 1 for pcm
        header.putShort(channels.toShort()) // number of channels
        header.putInt(longSampleRate)
        header.putInt(byteRate)
        header.putShort(((channels * bitsPerSample) / BITS_IN_BYTE).toShort()) // block align
        header.putShort(bitsPerSample.toShort()) // bits per sample
        header.put(DATA.toByteArray(Charsets.US_ASCII))
        header.putInt(totalAudioLength) // initial size

        header.flip()

        return header.array()
    }
}
