package org.wycliffeassociates.otter.jvm.app.widgets

import com.jfoenix.controls.JFXProgressBar
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.application.Platform
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*
import java.io.File
import java.util.concurrent.TimeUnit

class SimpleAudioPlayer(private val audioFile: File, private val player: IAudioPlayer) : HBox() {
    val playPauseButton = Button()
    val progressBar = JFXProgressBar()
    var playGraphic: Node? = null
        set (value) {
            field = value
            if (!isPlaying) {
                playPauseButton.graphic = field
            }
        }
    var pauseGraphic: Node? = null
        set (value) {
            field = value
            if (isPlaying) {
                playPauseButton.graphic = field
            }
        }

    private var isPlaying = false

    init {
        style {
            alignment = Pos.CENTER_LEFT
        }
        add(playPauseButton)
        add(progressBar)

        // Show indefinite loading bar
        progressBar.progress = -1.0

        player.load(audioFile).subscribe()

        // progress update observable
        var disposable: Disposable? = null

        player.addEventListener { audioEvent ->
            when (audioEvent) {
                AudioPlayerEvent.LOAD -> { progressBar.progress = 0.0 }
                AudioPlayerEvent.PLAY -> {
                    isPlaying = true
                    disposable = startProgressUpdate()
                    playPauseButton.graphic = pauseGraphic
                }
                AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> {
                    disposable?.dispose()
                    isPlaying = false
                    playPauseButton.graphic = playGraphic
                }
                AudioPlayerEvent.COMPLETE -> {
                    disposable?.dispose()
                    isPlaying = false
                    // Make sure we update on the main thread
                    // Only needed here since rest of events are triggered from FX thread
                    Platform.runLater {
                        progressBar.progress = 0.0
                        playPauseButton.graphic = playGraphic
                    }
                }
            }
        }

        playPauseButton.action {
            if (!isPlaying) player.play() else player.pause()
        }
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
                .interval(16, TimeUnit.MILLISECONDS)
                .subscribe {
                    val location = player
                            .getAbsoluteLocationInFrames()
                            .toDouble()
                    progressBar.progress = location / player.getAbsoluteDurationInFrames()
                }
    }

}

fun Pane.simpleaudioplayer(
        audioFile: File,
        audioPlayer: IAudioPlayer,
        init: SimpleAudioPlayer.() -> Unit
): SimpleAudioPlayer {
    val audioPlayer = SimpleAudioPlayer(audioFile, audioPlayer)
    audioPlayer.init()
    add(audioPlayer)
    return audioPlayer
}