package org.wycliffeassociates.otter.jvm.controls.controllers

import com.github.thomasnield.rxkotlinfx.observeOnFx
import org.slf4j.LoggerFactory
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Slider
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

private const val ANIMATION_REFRESH_MS = 16L

class AudioPlayerController(
    private val audioSlider: Slider,
    private var player: IAudioPlayer? = null
) {
    private val logger = LoggerFactory.getLogger(AudioPlayerController::class.java)

    private var startAtLocation = 0
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
                                _player.getAudioReader()?.seek(0)
                            }
                        }
                    }
                }
                isPlayingProperty.set(true)
            }
        }
    }

    fun load(player: IAudioPlayer) {
        audioSlider.value = 0.0
        audioSlider.max = player.getDurationInFrames().toDouble()
        this.player = player
    }

    private fun initializeSliderActions() {
        seek(0)
        audioSlider.value = 0.0
        audioSlider.setOnDragDetected {
            if (player?.isPlaying() == true) {
                resumeAfterDrag = true
                toggle()
            }
            dragging = true
        }
        audioSlider.setOnMouseClicked {
            val percent = max(0.0, min(it.x / audioSlider.width, 1.0))
            var wasPlaying = false
            if (player?.isPlaying() == true) {
                toggle()
                wasPlaying = true
            }
            seek(percentageToLocation(percent))
            if (wasPlaying) {
                toggle()
            }
        }
    }

    private fun startProgressUpdate(): Disposable {
        return Observable
            .interval(ANIMATION_REFRESH_MS, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in startProgressUpdate", e)
            }
            .subscribe {
                if (player?.isPlaying() == true && !audioSlider.isValueChanging && !dragging) {
                    audioSlider.value = playbackPosition().toDouble()
                }
            }
    }

    private fun play() {
        if (startAtLocation != 0) {
            seek(startAtLocation)
        }
        player?.play()
        startAtLocation = 0
    }

    fun pause() {
        player?.let {
            startAtLocation = it.getLocationInFrames()
            it.pause()
        }
    }

    fun seek(location: Int) {
        player?.let {
            it.seek(location)
            audioSlider.value = location.toDouble()
            if (!it.isPlaying()) {
                startAtLocation = location
            }
        } ?: run {
            startAtLocation = location
        }
    }

    private fun percentageToLocation(percent: Double): Int {
        var _percent = if (percent > 1.00) percent / 100F else percent
        player?.let {
            return (_percent * it.getDurationInFrames()).toInt()
        } ?: run {
            return 0
        }
    }

    private fun playbackPosition(): Int {
        return player?.let {
            it.getLocationInFrames() - it.frameStart
        } ?: 0
    }
}
