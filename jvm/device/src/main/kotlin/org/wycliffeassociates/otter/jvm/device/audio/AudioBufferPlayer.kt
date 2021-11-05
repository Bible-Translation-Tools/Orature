/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.device.audio

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.PitchShifter
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import com.jakewharton.rxrelay2.PublishRelay
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener


class AudioBufferPlayer(
    private val player: SourceDataLine,
    private val errorRelay: PublishRelay<AudioError> = PublishRelay.create()
) : IAudioPlayer {

    override val frameStart: Int
        get() = begin
    override val frameEnd: Int
        get() = end

    private var pause = AtomicBoolean(false)
    private var startPosition: Int = 0

    private var reader: AudioFileReader? = null

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
        reader = AudioFile(file).reader().let { _reader ->
            begin = 0
            end = _reader.totalFrames
            bytes = ByteArray(1024 * 6)
            listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
            _reader.open()
            _reader
        }
    }

    override fun loadSection(file: File, frameStart: Int, frameEnd: Int) {
        reader?.let { close() }
        begin = frameStart
        end = frameEnd
        reader = AudioFile(file).reader(frameStart, frameEnd).let { _reader ->
            bytes = ByteArray(_reader.sampleRate * _reader.channels)
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


                val processorFormat = TarsosDSPAudioFormat(44100f, 16, 1, true, false)
                startPosition = _reader.framePosition
                playbackThread = Thread {
                    try {
                        val event = AudioEvent(processorFormat)
                        val processDone = AtomicBoolean(false)
                        val callback = { processDone.set(true)
                            println("called back")}
                        val wsola = WaveformSimilarityBasedOverlapAdd(
                            WaveformSimilarityBasedOverlapAdd.Parameters.speechDefaults(
                                .6,
                                44100.toDouble()
                            )
                        )
                        player.open()
                        player.start()
                        while (_reader.hasRemaining() && !pause.get() && !playbackThread.isInterrupted) {
                            val written = _reader.getPcmBuffer(bytes)
                            val floats = FloatArray(bytes.size / 2)
                            TarsosDSPAudioFloatConverter.getConverter(processorFormat).toFloatArray(
                                bytes,
                                floats
                            )
                            event.floatBuffer = floats
                            wsola.process(event)
                            player.write(event.byteBuffer, 0, event.byteBuffer.size)
                        }
                        player.drain()
                        if (!pause.get()) {
                            startPosition = 0
                            listeners.forEach { it.onEvent(AudioPlayerEvent.COMPLETE) }
                            player.close()
                            seek(0)
                        }
                    } catch (e: LineUnavailableException) {
                        errorRelay.accept(AudioError(AudioErrorType.PLAYBACK, e))
                    } catch (e: IllegalArgumentException) {
                        errorRelay.accept(AudioError(AudioErrorType.PLAYBACK, e))
                    }

                }
                playbackThread.start()
            }
        }
    }

    override fun pause() {
        reader?.let { _reader ->
            val stoppedAt = getLocationInFrames()
            startPosition = stoppedAt
            pause.set(true)
            player.stop()
            player.flush()
            player.close()
            listeners.forEach { it.onEvent(AudioPlayerEvent.PAUSE) }
            _reader.seek(stoppedAt)
        }
    }

    override fun stop() {
        pause()
        seek(0)
        listeners.forEach { it.onEvent(AudioPlayerEvent.STOP) }
    }

    override fun close() {
        stop()
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

    override fun getDurationInFrames(): Int {
        return end - begin
    }

    override fun getDurationMs(): Int {
        return ((end - begin) / 44.1).toInt()
    }

    override fun getLocationInFrames(): Int {
        return startPosition + player.framePosition
    }

    override fun getLocationMs(): Int {
        return (getLocationInFrames() / 44.1).toInt()
    }
}
