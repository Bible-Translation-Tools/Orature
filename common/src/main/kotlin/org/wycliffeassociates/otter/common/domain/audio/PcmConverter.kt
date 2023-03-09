package org.wycliffeassociates.otter.common.domain.audio

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import java.io.File
import java.io.OutputStream

class PcmConverter(private val pcmFile: File, private val wavFile: File) {
    fun wavToPcm() {
        val wavReader = WavFile(wavFile).reader()
        val pcmWriter = PcmFile(pcmFile).writer(append = false)

        wavReader.open()
        val buffer = ByteArray(10240)
        while (wavReader.hasRemaining()) {
            val written = wavReader.getPcmBuffer(buffer)
            pcmWriter.write(buffer, 0, written)
        }

        cleanup(wavReader, pcmWriter)
    }

    fun pcmToWav() {
        val pcmReader = PcmFile(pcmFile).reader()
        val wavWriter = WavFile(
            wavFile,
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE
        ).writer(append = false)

        pcmReader.open()
        val buffer = ByteArray(10240)
        while (pcmReader.hasRemaining()) {
            val written = pcmReader.getPcmBuffer(buffer)
            wavWriter.write(buffer, 0, written)
        }

        cleanup(pcmReader, wavWriter)
    }

    private fun cleanup(reader: AudioFileReader, writer: OutputStream) {
        reader.release()
        writer.flush()
        writer.close()
    }
}