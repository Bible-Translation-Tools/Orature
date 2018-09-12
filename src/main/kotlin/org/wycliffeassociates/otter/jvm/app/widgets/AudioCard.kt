package org.wycliffeassociates.otter.jvm.app.widgets

import com.jfoenix.controls.JFXProgressBar
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import sun.audio.AudioPlayer
import tornadofx.action
import tornadofx.add
import tornadofx.hbox
import java.io.File
import java.util.concurrent.TimeUnit

class AudioCard(private val audioFile: File, private val player: IAudioPlayer) : VBox() {
    val title = Label()
    val subtitle = Label()
    val progress = JFXProgressBar()
    val playPauseButton = Button()
    private var isPlaying = false

    init {
        add(title)
        add(subtitle)
        hbox {
            add(playPauseButton)
            add(progress)
        }

        progress.progress = -1.0
        player
                .load(audioFile)
                .subscribe()

        var disposable: Disposable? = null
        // Add the player listener
        player.addEventListener {
            when (it) {
                AudioPlayerEvent.LOAD -> progress.progress = 0.0
                AudioPlayerEvent.PLAY -> {
                    playPauseButton.text = "Pause"
                    // start updating progress
                    disposable = Observable
                            .interval(15, TimeUnit.MILLISECONDS)
                            .observeOn(JavaFxScheduler.platform())
                            .subscribe {
                                progress.progress = player
                                        .getAbsoluteLocationInFrames()
                                        .toDouble() / player.getAbsoluteDurationInFrames()
                            }
                    isPlaying = true
                }
                AudioPlayerEvent.PAUSE, AudioPlayerEvent.STOP -> {
                    disposable?.dispose()
                    playPauseButton.text = "Play"
                    isPlaying = false
                }
                AudioPlayerEvent.COMPLETE -> {
                    disposable?.dispose()
                    playPauseButton.text = "Play"
                    isPlaying = false
                    progress.progress = 0.0
                }
            }
        }

        playPauseButton.action {
            if (isPlaying) player.pause() else player.play()
        }
    }
}

fun Pane.audiocard(audioFile: File, audioPlayer: IAudioPlayer, init: AudioCard.() -> Unit): AudioCard {
    val audioCard = AudioCard(audioFile, audioPlayer)
    audioCard.init()
    add(audioCard)
    return audioCard
}