package org.wycliffeassociates.otter.jvm.controls.media

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jfoenix.controls.JFXProgressBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*
import java.io.File
import java.util.concurrent.TimeUnit

// Named "Simple" since just displays a progress bar and play/pause button
// No waveform view
class SimpleAudioPlayer(private val audioFile: File, private val player: IAudioPlayer) : HBox() {

    private val logger = LoggerFactory.getLogger(SimpleAudioPlayer::class.java)

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
        Platform.runLater {
            if (!isPlaying.value) {
                if (!loaded) {
                    player.load(audioFile)
                    player.play()
                    loaded = true
                    return@runLater
                }
                player.play()
            } else player.pause()
        }
    }

    fun close() {
        player.close()
        loaded = false
    }

    fun refresh() {
        player.load(audioFile)
        loaded = true
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
            .interval(16, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in starting progress update", e)
            }
            .subscribe {
                val location = player
                    .getLocationInFrames()
                    .toDouble()
                progressBar.progress = location / player.getDurationInFrames()
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
