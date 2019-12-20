package org.wycliffeassociates.otter.jvm.device.audio

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioPlayerListener
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineEvent

class AudioPlayer : IAudioPlayer {

    // hold all the listeners
    private val listeners = mutableListOf<IAudioPlayerListener>()

    private var clip: Clip = AudioSystem.getClip()

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

    override fun load(file: File): Completable {
        pause()
        if (clip.isOpen) {
            clip.flush()
            clip.close()
        }
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        val info = DataLine.Info(Clip::class.java, audioInputStream.format)
        clip = AudioSystem.getLine(info) as Clip
        clip.addLineListener { lineEvent ->
            if (lineEvent.type == LineEvent.Type.STOP && clip.framePosition == clip.frameLength) {
                listeners.forEach { it.onEvent(AudioPlayerEvent.COMPLETE) }
                // Rewind file
                clip.framePosition = 0
            }
        }
        return Completable.fromAction {
            clip.open(audioInputStream)
            listeners.forEach { it.onEvent(AudioPlayerEvent.LOAD) }
        }.subscribeOn(Schedulers.io())
    }

    override fun play() {
        if (!clip.isRunning && clip.frameLength > 0) {
            clip.start()
            listeners.forEach { it.onEvent(AudioPlayerEvent.PLAY) }
        }
    }

    override fun pause() {
        if (clip.isRunning) {
            clip.stop()
            // Bug in Clip implementation.
            // Need this to make sure LineListener events are sent after resume
            clip.framePosition = clip.framePosition
            listeners.forEach { it.onEvent(AudioPlayerEvent.PAUSE) }
        }
    }

    override fun stop() {
        if (clip.isRunning) {
            clip.stop()
            listeners.forEach { it.onEvent(AudioPlayerEvent.STOP) }
        }
        clip.framePosition = 0
    }

    override fun close() {
        stop()
        clip.close()
    }

    override fun getAbsoluteDurationInFrames(): Int {
        return clip.frameLength
    }

    override fun getAbsoluteDurationMs(): Int {
        return (getAbsoluteDurationInFrames() / 44.1).toInt()
    }

    override fun getAbsoluteLocationInFrames(): Int {
        return clip.framePosition
    }

    override fun getAbsoluteLocationMs(): Int {
        return (getAbsoluteLocationInFrames() / 44.1).toInt()
    }
}