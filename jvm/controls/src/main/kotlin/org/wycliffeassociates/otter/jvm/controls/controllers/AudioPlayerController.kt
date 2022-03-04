/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.controllers

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Slider
import javafx.scene.input.KeyCode
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import tornadofx.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

const val DURATION_FORMAT = "%02d:%02d" // mm:ss
private const val ANIMATION_REFRESH_MS = 16L

class AudioPlayerController(
    private val audioSlider: Slider,
    private var player: IAudioPlayer? = null
) {
    private val logger = LoggerFactory.getLogger(AudioPlayerController::class.java)

    private var startAtLocation = 0
    private var disposable: Disposable? = null
    private var resumeAfterDrag = false

    val isPlayingProperty = SimpleBooleanProperty(false)
    val playbackRateProperty = SimpleDoubleProperty(1.0)

    init {
        initializeSliderActions()

        playbackRateProperty.onChange {
            setPlaybackRate(it)
        }
    }

    fun toggle() {
        player?.let { _player ->
            if (_player.isPlaying()) {
                pause()
            } else {
                play()
            }
        }
    }

    fun setPlaybackRate(rate: Double) {
        player?.let { _player ->
            var wasPlaying = false
            if (_player.isPlaying()) {
                _player.pause()
                _player.seek(Integer.max(_player.getLocationInFrames(), 0))
                wasPlaying = true
            }
            _player.changeRate(rate)
            if (wasPlaying) {
                _player.play()
            }
        }
    }

    fun load(player: IAudioPlayer) {
        audioSlider.value = 0.0
        audioSlider.max = player.getDurationInFrames().toDouble()
        startAtLocation = 0

        this.player = player
        disposable?.dispose()
        disposable = startProgressUpdate()

        setPlaybackRate(playbackRateProperty.value)

        player.addEventListener {
            if (
                it == AudioPlayerEvent.PAUSE ||
                it == AudioPlayerEvent.STOP ||
                it == AudioPlayerEvent.COMPLETE
            ) {
                Platform.runLater {
                    isPlayingProperty.set(false)
                    when (it) {
                        AudioPlayerEvent.COMPLETE -> {
                            audioSlider.value = 0.0
                            startAtLocation = 0
                            player.getAudioReader()?.seek(0)
                        }
                    }
                }
            }
        }
    }

    private fun initializeSliderActions() {
        seek(0)
        audioSlider.value = 0.0
        audioSlider.setOnDragDetected {
            if (player?.isPlaying() == true) {
                resumeAfterDrag = true
                toggle()
            }
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
            if (resumeAfterDrag) {
                resumeAfterDrag = false
                toggle()
            }
        }
        audioSlider.setOnKeyPressed {
            when (it.code) {
                KeyCode.LEFT, KeyCode.RIGHT -> {
                    if (player?.isPlaying() == true) {
                        resumeAfterDrag = true
                        toggle()
                    }
                    seekInterval(it.code)
                }
            }
        }
        audioSlider.setOnKeyReleased {
            when (it.code) {
                KeyCode.LEFT, KeyCode.RIGHT -> {
                    if (resumeAfterDrag) {
                        resumeAfterDrag = false
                        toggle()
                    }
                }
                KeyCode.ENTER, KeyCode.SPACE -> {
                    toggle()
                    it.consume()
                }
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
                if (player?.isPlaying() == true) {
                    isPlayingProperty.set(true)
                } else {
                    isPlayingProperty.set(false)
                }
                if (player?.isPlaying() == true && !audioSlider.isValueChanging) {
                    audioSlider.value = playbackPosition().toDouble()
                }
            }
    }

    private fun play() {
        isPlayingProperty.set(true)
        if (startAtLocation != 0) {
            seek(startAtLocation)
        }
        player?.play()
        startAtLocation = 0
    }

    fun pause() {
        isPlayingProperty.set(false)
        player?.let {
            startAtLocation = it.getLocationInFrames()
            it.pause()
        }
    }

    fun seek(location: Int) {
        audioSlider.value = location.toDouble()

        player?.let {
            it.seek(location)
            if (!it.isPlaying()) {
                startAtLocation = location
            }
        } ?: run {
            startAtLocation = location
        }
    }

    private fun percentageToLocation(percent: Double): Int {
        val _percent = if (percent > 1.00) percent / 100F else percent
        player?.let {
            return (_percent * it.getDurationInFrames()).toInt()
        } ?: run {
            return 0
        }
    }

    private fun playbackPosition(): Int {
        return player?.getLocationInFrames() ?: 0
    }

    private fun seekInterval(keyCode: KeyCode) {
        player?.let {
            val interval = percentageToLocation(10.0)
            var location = it.getLocationInFrames()
            when (keyCode) {
                KeyCode.LEFT -> location -= interval
                KeyCode.RIGHT -> location += interval
            }
            seek(max(0, location))
        }
    }
}

fun framesToTimecode(value: Double, sampleRate: Int = DEFAULT_SAMPLE_RATE): String {
    val framesPerMs = if (sampleRate > 0) {
        sampleRate / 1000
    } else {
        DEFAULT_SAMPLE_RATE / 1000
    }
    val durationMs = (value / framesPerMs).toLong()
    val min = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val sec = TimeUnit.MILLISECONDS.toSeconds(durationMs) % 60
    return DURATION_FORMAT.format(min, sec)
}
