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
import com.sun.javafx.util.Utils
import io.reactivex.Observable
import java.lang.ref.WeakReference
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Slider
import javafx.scene.input.KeyCode
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.utils.simulateKeyPress
import tornadofx.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

const val DURATION_FORMAT = "%02d:%02d" // mm:ss
private const val ANIMATION_REFRESH_MS = 32L // 30fps
const val SEEK_INTERVAL = 0.1
const val FAST_SEEK_INTERVAL = 1.0

enum class ScrollSpeed {
    NORMAL,
    FAST
}

class AudioPlayerController(
    var audioSlider: Slider? = null,
    private var player: IAudioPlayer? = null
) {
    private val logger = LoggerFactory.getLogger(AudioPlayerController::class.java)

    private var startAtLocation = 0
    private var resumeAfterDrag = false

    val isPlayingProperty = SimpleBooleanProperty(false)
    val playbackRateProperty = SimpleDoubleProperty(1.0)

    private var timerListener: ITimerListener? = null

    init {
        initializeSliderActions()

        playbackRateProperty.onChange {
            setPlaybackRate(it)
        }
    }

    fun release() {
        player?.close()
        player = null
        startAtLocation = 0
        isPlayingProperty.set(false)
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
        audioSlider?.value = 0.0
        audioSlider?.max = player.getDurationInFrames().toDouble()
        startAtLocation = 0
        this.player = player
        setPlaybackRate(playbackRateProperty.value)

        subscribeToTimer()

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
                            audioSlider?.value = 0.0
                            startAtLocation = 0
                            player.getAudioReader()?.seek(0)
                        }
                    }
                }
            } else if (it == AudioPlayerEvent.PLAY) {
                Platform.runLater {
                    isPlayingProperty.set(true)
                }
            }
        }
    }

    private fun subscribeToTimer() {
        timerListener = object : ITimerListener {
            override var isGarbageCollected: Boolean = false
            override fun onTick() {
                if (isPlayingProperty.value == true && audioSlider?.isValueChanging == false) {
                    audioSlider?.value = playbackPosition().toDouble()
                }
            }
        }.also { SliderTimer.addListener(it) }
    }

    private fun initializeSliderActions() {
        seek(0)
        audioSlider?.value = 0.0
        audioSlider?.setOnDragDetected {
            if (player?.isPlaying() == true) {
                resumeAfterDrag = true
                toggle()
            }
        }
        audioSlider?.setOnMouseClicked {
            val percent = max(0.0, min(it.x / audioSlider!!.width, 1.0)) * 100
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
        audioSlider?.setOnKeyPressed {
            val speed = if (it.isControlDown) ScrollSpeed.FAST else ScrollSpeed.NORMAL
            when (it.code) {
                KeyCode.LEFT -> {
                    rewind(speed)
                    it.consume()
                }
                KeyCode.RIGHT -> {
                    fastForward(speed)
                    it.consume()
                }
                KeyCode.DOWN -> audioSlider?.simulateKeyPress(KeyCode.TAB)
                KeyCode.UP -> audioSlider?.simulateKeyPress(KeyCode.TAB, shiftDown = true)
            }
        }
        audioSlider?.setOnKeyReleased {
            when (it.code) {
                KeyCode.LEFT, KeyCode.RIGHT -> {
                    if (resumeAfterDrag) {
                        resumeAfterDrag = false
                        toggle()
                    }
                    it.consume()
                }
                KeyCode.ENTER, KeyCode.SPACE -> {
                    toggle()
                    it.consume()
                }
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
        audioSlider?.value = location.toDouble()

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
        val _percent = percent / 100F
        player?.let {
            return (_percent * it.getDurationInFrames()).toInt()
        } ?: run {
            return 0
        }
    }

    private fun playbackPosition(): Int {
        return player?.getLocationInFrames() ?: 0
    }

    private fun seekInterval(keyCode: KeyCode, speed: ScrollSpeed) {
        player?.let {
            if (it.isPlaying()) {
                resumeAfterDrag = true
                toggle()
            }

            val percent = if (speed == ScrollSpeed.FAST) FAST_SEEK_INTERVAL else SEEK_INTERVAL
            val interval = percentageToLocation(percent)
            var location = it.getLocationInFrames()
            when (keyCode) {
                KeyCode.LEFT -> location -= interval
                KeyCode.RIGHT -> location += interval
            }
            seek(Utils.clamp(0, location, it.getDurationInFrames()))
        }
    }

    fun rewind(speed: ScrollSpeed) {
        seekInterval(KeyCode.LEFT, speed)
    }

    fun fastForward(speed: ScrollSpeed) {
        seekInterval(KeyCode.RIGHT, speed)
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

fun remainingTimecode(
    progressValue: Double,
    durationMs: Int,
    sampleRate: Int = DEFAULT_SAMPLE_RATE
): String {
    val framesPerMs = if (sampleRate > 0) {
        sampleRate / 1000
    } else {
        DEFAULT_SAMPLE_RATE / 1000
    }
    val remaining = durationMs - (progressValue / framesPerMs).toLong()
    val min = max(0, TimeUnit.MILLISECONDS.toMinutes(remaining))
    val sec = max(0, TimeUnit.MILLISECONDS.toSeconds(remaining) % 60)
    return DURATION_FORMAT.format(min, sec)
}

private interface ITimerListener {
    var isGarbageCollected: Boolean
    fun onTick()
}

private object SliderTimer {
    class WeakTimerListener(listener: ITimerListener) : ITimerListener {
        override var isGarbageCollected = false
        private val wref = WeakReference(listener)

        override fun onTick() {
            wref.get()?.onTick() ?: kotlin.run { isGarbageCollected = true }
        }
    }

    private val listeners = mutableListOf<ITimerListener>()

    init {
        startTimer()
    }

    private fun startTimer() {
        Observable
            .interval(ANIMATION_REFRESH_MS, TimeUnit.MILLISECONDS)
            .observeOnFx()
            .subscribe {
                synchronized(SliderTimer) {
                    listeners.removeIf { it.isGarbageCollected }
                    listeners.forEach { it.onTick() }
                }
            }
    }

    fun addListener(listener: ITimerListener) {
        synchronized(SliderTimer) {
            listeners.add(WeakTimerListener(listener))
        }
    }
}
