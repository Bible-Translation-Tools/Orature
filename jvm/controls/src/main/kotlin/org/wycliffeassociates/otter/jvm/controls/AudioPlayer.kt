package org.wycliffeassociates.otter.jvm.controls

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Control
import javafx.scene.control.Skin
import org.wycliffeassociates.otter.jvm.controls.skins.AudioPlayerSkin
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine

class AudioPlayer(private val audioFile: File?) : Control() {

    var loaded = false
    var startAtPercent = 0F
    lateinit var clip: Clip

    val isPlaying: Boolean
        get() = if (::clip.isInitialized) clip.isRunning else false

    val playbackPercentage = SimpleDoubleProperty(0.0)

    val sourceAvailable = audioFile?.exists() ?: false

    fun load() {
        if (audioFile != null && audioFile.exists()) {
            val stream = AudioSystem.getAudioInputStream(audioFile)
            val format = stream.format
            val info = DataLine.Info(Clip::class.java, format)
            clip = AudioSystem.getLine(info) as Clip
            clip.open(stream)
            clip.microsecondPosition = (clip.microsecondLength * startAtPercent).toLong()
            loaded = true
            startAtPercent = 0F
        }
    }

    fun play() {
        if (!loaded) {
            println("wasn't loaded")
            load()
        }
        clip.start()
    }

    fun pause() {
        clip.stop()
    }

    fun seek(percent: Float) {
        if (::clip.isInitialized) {
            var resume = false
            if (clip.isRunning) {
                resume = true
                clip.stop()
            }
            clip.microsecondPosition = (clip.microsecondLength * percent).toLong()
            if (resume) {
                clip.stop()
            }
        } else {
            startAtPercent = percent
        }
    }

    fun playbackPosition(): Double {
        return if (::clip.isInitialized) {
            (clip.framePosition.toDouble() / clip.frameLength).times(100)
        } else {
            0.0
        }
    }

    override fun createDefaultSkin(): Skin<*> {
        return AudioPlayerSkin(this)
    }
}