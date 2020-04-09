package org.wycliffeassociates.otter.jvm.controls.controllers

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import java.util.concurrent.TimeUnit

class AudioPlayerController(
    private var player: IAudioPlayer?,
    private val audioSlider: Slider
) {

    private var startAtPercent = 0F
    private var disposable: Disposable? = null
    private var dragging = false
    private var resumeAfterDrag = false

    val isPlayingProperty = SimpleBooleanProperty(false)

    init {
        initializeSliderActions()
    }

    fun toggle() {
        disposable?.dispose()
        player?.let { _player ->
            if (_player.isPlaying()) {
                pause()
            } else {
                disposable = startProgressUpdate()
                play()
                _player.addEventListener {
                    if (
                        it == AudioPlayerEvent.PAUSE ||
                        it == AudioPlayerEvent.STOP ||
                        it == AudioPlayerEvent.COMPLETE
                    ) {
                        disposable?.dispose()
                        Platform.runLater {
                            isPlayingProperty.set(false)
                            if (it == AudioPlayerEvent.COMPLETE) {
                                audioSlider.value = 0.0
                            }
                        }
                    }
                }
                isPlayingProperty.set(true)
            }
        }
    }

    fun load(player: IAudioPlayer) {
        this.player = player
    }

    fun initializeSliderActions() {
        audioSlider.value = 0.0
        audioSlider.setOnDragDetected {
            if (player?.isPlaying() == true) {
                resumeAfterDrag = true
                toggle()
            }
            dragging = true
        }
        audioSlider.setOnMouseReleased {
            seek(audioSlider.value.toFloat() / 100F)
            if (dragging) {
                dragging = false
                if (resumeAfterDrag) {
                    toggle()
                    resumeAfterDrag = false
                }
            }
        }
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
            .interval(20, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .subscribe {
                if (player?.isPlaying() == true && !audioSlider.isValueChanging && !dragging) {
                    audioSlider.value = playbackPosition()
                }
            }
    }

    private fun play() {
        seek(startAtPercent)
        player?.play()
        startAtPercent = 0F
    }

    private fun pause() {
        player?.let {
            startAtPercent = it.getAbsoluteLocationInFrames() / it.getAbsoluteDurationInFrames().toFloat()
            it.pause()
        }
    }

    private fun seek(percent: Float) {
        player?.let {
            val position = (it.getAbsoluteDurationInFrames() * percent).toInt()
            it.seek(position)
            if (!it.isPlaying()) {
                startAtPercent = percent
            }
        } ?: run { startAtPercent = percent }
    }

    private fun playbackPosition(): Double {
        return player?.let {
            val position = it.getAbsoluteLocationInFrames()
            val total = it.getAbsoluteDurationInFrames()
            (position / total.toDouble()).times(100)
        } ?: 0.0
    }
}