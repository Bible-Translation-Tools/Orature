package device.audio

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import device.IAudioPlayer

class AudioPlayer: IAudioPlayer {

    private var clip: Clip = AudioSystem.getClip()

    fun load(file: File): Completable {
        pause()
        if (clip.isOpen) clip.close()
        clip = AudioSystem.getClip()
        val audioInputStream = AudioSystem.getAudioInputStream(file)
        return Completable.fromAction {
            clip.open(audioInputStream)
        }.subscribeOn(Schedulers.io())
    }

    fun play() {
        if (!clip.isRunning) clip.start()
    }

    fun pause() {
        if (clip.isRunning) clip.stop()
    }

    fun stop() {
        pause()
        clip.framePosition = 0
    }

    fun getAbsoluteDurationInFrames(): Int {
        return clip.frameLength
    }

    fun getAbsoluteDurationMs(): Int {
        return (getAbsoluteDurationInFrames() / 44.1).toInt()
    }

    fun getAbsoluteLocationInFrames(): Int {
        return clip.framePosition
    }

    fun getAbsoluteLocationMs(): Int {
        return (getAbsoluteLocationInFrames() / 44.1).toInt()
    }
}