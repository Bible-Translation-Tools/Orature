package org.wycliffeassociates.otter.jvm.device.audio

import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener
import org.wycliffeassociates.otter.common.io.AudioFileReader
import org.wycliffeassociates.otter.common.io.wav.WavFile
import org.wycliffeassociates.otter.common.io.wav.WavFileReader
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioBufferPlayer() : IAudioPlayer {

    private var pause = false
    private var startPosition: Int = 0

    private lateinit var reader: AudioFileReader
    private lateinit var player: SourceDataLine
    private lateinit var bytes: ByteArray
    private lateinit var playbackThread: Thread

    private val listeners = mutableListOf<IAudioPlayerListener>()

    override fun addEventListener(listener: IAudioPlayerListener) {
        listeners.add(listener)
    }

    override fun addEventListener(onEvent: (event: AudioPlayerEvent) -> Unit) {
        listeners.add(
            object : IAudioPlayerListener {
                override fun onEvent(event: AudioPlayerEvent) {
                    onEvent(event)
                }
            }
        )
    }

    override fun load(file: File) {
        reader = WavFileReader(WavFile(file))
        bytes = ByteArray(reader.sampleRate * reader.channels)
        player = AudioSystem.getSourceDataLine(
            AudioFormat(
                reader.sampleRate.toFloat(),
                reader.sampleSize,
                reader.channels,
                true,
                false
            )
        )
        listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
        player.open()
    }

    override fun loadSection(file: File, frameStart: Int, frameEnd: Int) {
        reader = WavFileReader(WavFile(file), frameStart, frameEnd)
        bytes = ByteArray(reader.sampleRate * reader.channels)
        player = AudioSystem.getSourceDataLine(
            AudioFormat(
                reader.sampleRate.toFloat(),
                reader.sampleSize,
                reader.channels,
                true,
                false
            )
        )
        listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
        player.open()
    }

    override fun play() {
        if (!player.isActive) {
            pause = false
            startPosition = reader.framePosition
            playbackThread = Thread {
                player.open()
                player.start()
                while (reader.hasRemaining() && !pause && !playbackThread.isInterrupted) {
                    val written = reader.getPcmBuffer(bytes)
                    player.write(bytes, 0, written)
                }
                player.close()
                if (!pause) {
                    listeners.forEach { it.onEvent(AudioPlayerEvent.COMPLETE) }
                }
            }
            playbackThread.start()
        }
    }

    override fun pause() {
        val stoppedAt = getAbsoluteLocationInFrames()
        pause = true
        player.stop()
        player.flush()
        listeners.forEach { it.onEvent(AudioPlayerEvent.PAUSE) }
        reader.seek(stoppedAt)
    }

    override fun stop() {
        pause()
        player.flush()
        listeners.forEach { it.onEvent(AudioPlayerEvent.STOP) }
    }

    override fun close() {
        player.close()
    }

    override fun seek(position: Int) {
        val resume = player.isActive
        player.stop()
        if (::playbackThread.isInitialized) {
            playbackThread.interrupt()
        }
        player.flush()
        startPosition = position
        reader.seek(position)
        if (resume) {
            play()
        }
    }

    override fun isPlaying(): Boolean {
        return player.isRunning
    }

    override fun getAbsoluteDurationInFrames(): Int {
        return reader.totalFrames
    }

    override fun getAbsoluteDurationMs(): Int {
        return (reader.totalFrames / 44.1).toInt()
    }

    override fun getAbsoluteLocationInFrames(): Int {
        return startPosition + player.framePosition
    }

    override fun getAbsoluteLocationMs(): Int {
        return (getAbsoluteLocationInFrames() / 44.1).toInt()
    }

    fun framePosition() {
        startPosition + player.framePosition
    }
}