package org.wycliffeassociates.otter.jvm.markerapp.audio

import java.lang.Exception
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioBufferPlayer(val reader: AudioFileReader) {

    lateinit var player: SourceDataLine

    init {
        AudioSystem.getMixerInfo().forEach { println(it) }
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
            Thread {
                try {
                    player.open()
                    val bytes = ByteArray(reader.sampleRate * reader.channels)
                    println(player.format)
                    player.start()
                    while (reader.hasRemaining()) {
                        val written = reader.getPcmBuffer(bytes)
                        player.write(bytes, 0, written)
                    }
                    player.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}