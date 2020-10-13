package org.wycliffeassociates.otter.jvm.device.audio

import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.SourceDataLine

class AudioBufferPlayer : IAudioPlayer {

    private var pause = AtomicBoolean(false)
    private var startPosition: Int = 0

    private var reader: AudioFileReader? = null
    private lateinit var player: SourceDataLine
    private lateinit var bytes: ByteArray
    private lateinit var playbackThread: Thread
    private var begin = 0
    private var end = 0

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
        reader?.let { close() }
        reader = WavFileReader(WavFile(file)).let { _reader ->
            begin = 0
            end = _reader.totalFrames
            bytes = ByteArray(_reader.sampleRate * _reader.channels)
            player = AudioSystem.getSourceDataLine(
                AudioFormat(
                    _reader.sampleRate.toFloat(),
                    _reader.sampleSize,
                    _reader.channels,
                    true,
                    false
                )
            )
            listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
            _reader.open()
            _reader
        }
    }

    override fun loadSection(file: File, frameStart: Int, frameEnd: Int) {
        reader?.let { close() }
        begin = frameStart
        end = frameEnd
        reader = WavFileReader(WavFile(file), frameStart, frameEnd).let { _reader ->
            bytes = ByteArray(_reader.sampleRate * _reader.channels)
            player = AudioSystem.getSourceDataLine(
                AudioFormat(
                    _reader.sampleRate.toFloat(),
                    _reader.sampleSize,
                    _reader.channels,
                    true,
                    false
                )
            )
            listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
            _reader.open()
            _reader
        }
    }

    override fun getAudioReader(): AudioFileReader? {
        return reader
    }

    override fun play() {
        reader?.let { _reader ->
            if (!player.isActive) {
                listeners.forEach { it.onEvent(AudioPlayerEvent.PLAY) }
                pause.set(false)
                startPosition = _reader.framePosition
                playbackThread = Thread {
                    player.open()
                    player.start()
                    while (_reader.hasRemaining() && !pause.get() && !playbackThread.isInterrupted) {
                        val written = _reader.getPcmBuffer(bytes)
                        player.write(bytes, 0, written)
                    }
                    player.drain()
                    if (!pause.get()) {
                        startPosition = 0
                        listeners.forEach { it.onEvent(AudioPlayerEvent.COMPLETE) }
                        player.close()
                    }
                }
                playbackThread.start()
            }
        }
    }

    override fun pause() {
        reader?.let { _reader ->
            if (::player.isInitialized) {
                val stoppedAt = getAbsoluteLocationInFrames()
                startPosition = stoppedAt
                pause.set(true)
                player.stop()
                player.flush()
                player.close()
                listeners.forEach { it.onEvent(AudioPlayerEvent.PAUSE) }
                _reader.seek(stoppedAt)
            }
        }
    }

    override fun stop() {
        pause()
        listeners.forEach { it.onEvent(AudioPlayerEvent.STOP) }
    }

    override fun close() {
        if (::player.isInitialized) {
            player.close()
        }
        if (reader != null) {
            reader?.release()
            reader = null
        }
    }

    override fun seek(position: Int) {
        val resume = player.isActive
        player.stop()
        if (::playbackThread.isInitialized) {
            playbackThread.interrupt()
        }
        player.flush()
        startPosition = position
        reader?.seek(position)
        if (resume) {
            play()
        }
    }

    override fun isPlaying(): Boolean {
        return player.isRunning
    }

    override fun getAbsoluteDurationInFrames(): Int {
        return end - begin
    }

    override fun getAbsoluteDurationMs(): Int {
        return ((end - begin) / 44.1).toInt()
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
