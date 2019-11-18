package org.wycliffeassociates.otter.jvm.markerapp.audio

import java.lang.Exception
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioBufferPlayer(val reader: AudioFileReader) {

    private val bytes = ByteArray(reader.sampleRate * reader.channels)
    private var startPosition: Int = 0

    lateinit var player: SourceDataLine
    lateinit var playbackThread: Thread

    init {
        try {
            player = AudioSystem.getSourceDataLine(
                    AudioFormat(
                            reader.sampleRate.toFloat(),
                            reader.sampleSize,
                            reader.channels,
                            true,
                            false
                    )
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun play() {
        if (!player.isActive && !player.isOpen) {
            startPosition = reader.framePosition
            playbackThread = Thread {
                player.open()
                player.start()
                while (reader.hasRemaining()) {
                    val written = reader.getPcmBuffer(bytes)
                    player.write(bytes, 0, written)
                }
                player.close()
            }
            playbackThread.start()
        }
    }

    fun framePosition() {
        startPosition + player.framePosition
    }
}