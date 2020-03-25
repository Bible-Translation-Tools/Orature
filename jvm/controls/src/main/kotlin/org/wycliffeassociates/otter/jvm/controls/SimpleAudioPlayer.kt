package org.wycliffeassociates.otter.jvm.controls

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXProgressBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*
import java.io.File
import java.util.concurrent.TimeUnit

// Named "Simple" since just displays a progress bar and play/pause button
// No waveform view
class SimpleAudioPlayer(private val audioFile: File, private val player: IAudioPlayer) : HBox() {

    val progressBar = JFXProgressBar()
    val isPlaying = SimpleBooleanProperty(false)
    var loaded = false

    init {
        style {
            alignment = Pos.TOP_CENTER
        }
        add(progressBar)

        progressBar.progress = 0.0
        progressBar.hgrow = Priority.ALWAYS
        progressBar.maxWidth = Double.MAX_VALUE

        // progress update observable
        var disposable: Disposable? = null

        player.addEventListener { audioEvent ->
            when (audioEvent) {
                AudioPlayerEvent.LOAD -> {
                    Platform.runLater { progressBar.progress = 0.0 }
                }
                AudioPlayerEvent.PLAY -> {
                    isPlaying.set(true)
                    disposable = startProgressUpdate()
                }
                AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> {
                    disposable?.dispose()
                    isPlaying.set(false)
                }
                AudioPlayerEvent.COMPLETE -> {
                    disposable?.dispose()
                    // Make sure we update on the main thread
                    // Only needed here since rest of events are triggered from FX thread
                    Platform.runLater {
                        progressBar.progress = 0.0
                        isPlaying.set(false)
                    }
                }
            }
        }
    }

    fun buttonPressed() {
        if (!isPlaying.value) {
            if (!loaded) {
                player.load(audioFile)
                Platform.runLater {
                    player.play()
                    loaded = true
                }
            }
            player.seek(0)
            player.play()
        } else player.pause()
    }

    fun close() {
        player.close()
        loaded = false
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
            .interval(16, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .subscribe {
                val location = player
                    .getAbsoluteLocationInFrames()
                    .toDouble()
                progressBar.progress = location / player.getAbsoluteDurationInFrames()
            }
    }
}

fun simpleaudioplayer(
    audioFile: File,
    audioPlayer: IAudioPlayer,
    init: SimpleAudioPlayer.() -> Unit
): SimpleAudioPlayer {
    return SimpleAudioPlayer(audioFile, audioPlayer)
        .apply {
            init()
        }
}
