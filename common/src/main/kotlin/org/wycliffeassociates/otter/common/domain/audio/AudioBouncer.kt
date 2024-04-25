package org.wycliffeassociates.otter.common.domain.audio

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AudioBouncer @Inject constructor() {

    private val logger = LoggerFactory.getLogger(AudioBouncer::class.java)

    val isInterrupted = AtomicBoolean(false)

    /**
     * Constructs a Wav file at the path specified by bouncedAudio and with data given by the reader and markers
     *
     * @param  bouncedAudio  a File specifying where audio data is to be stored
     * @param  reader an AudioFileReader used to retrieve source audio
     * @param  markers list of AudioMarkers to be added to the wav file metadata
     */
    @Synchronized
    fun bounceAudio(bouncedAudio: File, reader: AudioFileReader, markers: List<AudioMarker>) {
        logger.info("Started bouncing audio for ${markers}")
        if (isInterrupted.get()) isInterrupted.set(false)

        val bytes = ByteArray(DEFAULT_BUFFER_SIZE)

        reader.use {
            reader.open()
            reader.seek(0)

            if (bouncedAudio.exists() && bouncedAudio.length() > 0) {
                bouncedAudio.delete()
            }

            val wav = WavFile(bouncedAudio, reader.channels, reader.sampleRate, reader.sampleSizeBits)
            WavOutputStream(wav, buffered = true).use { out ->
                while (reader.hasRemaining() && !isInterrupted.get()) {
                    val read = reader.getPcmBuffer(bytes)
                    out.write(bytes, 0, read)
                }
            }

            if (!isInterrupted.get()) {
                wav.update()
                val oaf = OratureAudioFile(bouncedAudio)
                for (verse in markers) {
                    oaf.addMarker<AudioMarker>(verse.clone())
                }
                oaf.update()
            }

            if (isInterrupted.get()) {
                logger.info("interrupted bouncing audio")
            } else {
                logger.info("Finished bouncing audio for ${markers}")
            }
            isInterrupted.set(false)
        }
    }

    fun interrupt() {
        isInterrupted.set(true)
    }
}




