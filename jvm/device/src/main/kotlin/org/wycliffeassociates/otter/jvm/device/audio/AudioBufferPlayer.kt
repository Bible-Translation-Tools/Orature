package org.wycliffeassociates.otter.jvm.device.audio

import org.wycliffeassociates.otter.common.io.AudioFileReader
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioBufferPlayer(private val reader: AudioFileReader) {

    private val bytes = ByteArray(reader.sampleRate * reader.channels)
    private var startPosition: Int = 0

    private var player: SourceDataLine = AudioSystem.getSourceDataLine(
        AudioFormat(
            reader.sampleRate.toFloat(),
            reader.sampleSize,
            reader.channels,
            true,
            false
        )
    )
    private lateinit var playbackThread: Thread

    init {
        player.open()
    }

    fun play() {
        if (!player.isActive) {
            startPosition = reader.framePosition
            playbackThread = Thread {
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

    fun close() {
        player.close()
    }

    fun seek(frame: Int) {
        val resume = player.isActive
        player.stop()
        if (::playbackThread.isInitialized) {
            playbackThread.interrupt()
        }
        player.flush()
        startPosition = frame
        reader.seek(frame)
        if (resume) {
            play()
        }
    }

    fun framePosition() {
        startPosition + player.framePosition
    }
}