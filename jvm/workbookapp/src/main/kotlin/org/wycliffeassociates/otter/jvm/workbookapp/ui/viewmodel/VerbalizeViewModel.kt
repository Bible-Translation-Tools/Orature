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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.util.Duration
import javax.inject.Inject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.device.AudioPlayerEvent
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.keyframe
import tornadofx.setValue
import tornadofx.timeline

class VerbalizeViewModel : ViewModel() {
    private val logger = LoggerFactory.getLogger(VerbalizeViewModel::class.java)

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    var wav: AudioFile? = null

    var player: IAudioPlayer? = null
    var recorder: IAudioRecorder? = null
    var writer: WavFileWriter? = null

    val isPlayingProperty = SimpleBooleanProperty(false)
    var isLoaded = false

    val hasContentProperty = SimpleBooleanProperty(false)

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty

    var hasWrittenProperty = SimpleBooleanProperty(false)
    var hasWritten by hasWrittenProperty

    val canSaveProperty: BooleanBinding = (recordingProperty.not()).and(hasWrittenProperty)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDock() {
        prepareAudio()
    }

    private fun prepareAudio() {
        prepareVerbalizationFile()
        getAudioConnections()
        prepareRecorder()
    }

    private fun prepareVerbalizationFile() {
        wav = AudioFile(
            directoryProvider.createTempFile("verbalize",".wav").apply { deleteOnExit() },
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE
        )
    }

    private fun getAudioConnections() {
        recorder = audioConnectionFactory.getRecorder()
        player = audioConnectionFactory.getPlayer()
    }

    private fun prepareRecorder() {
        recorder?.let { recorder ->
            writer = WavFileWriter(wav!!, recorder.getAudioStream()) {
                Platform.runLater {
                    logger.info("Writer has stopped, setting content for Verbalize")
                }
            }
        }
    }

    fun toggle() {
        if (isRecording) {
            logger.info("Pausing Verbalization")
            stop()
            recorder?.stop()
            writer?.pause()
            hasWritten = true
            hasContentProperty.set(true)
        } else {
            animate()
            recorder?.let {
                logger.info("Recording Verbalization")
                it.start()
                writer?.start()
            }
        }
        isRecording = !isRecording
    }

    fun reRecord() {
        hasContentProperty.set(false)
        if (isLoaded && player?.isPlaying() == true) {
            isPlayingProperty.set(false)
            player?.pause()
        }
        player!!.release()
        isLoaded = false
        wav?.file?.let {
            logger.info("Deleting verbalization.")
            it.delete()
        } ?: run {
            logger.error("Could not delete file for rerecording verbalization.")
        }
        prepareAudio()
        toggle()
    }

    fun playToggle() {
        player?.let { player ->
            if (!isLoaded) {
                wav?.let { wav ->
                    player.load(wav.file)
                    player.addEventListener {
                        when (it) {
                            AudioPlayerEvent.COMPLETE -> {
                                Platform.runLater {
                                    isPlayingProperty.set(false)
                                }
                            }
                        }
                    }
                }
                isLoaded = true
            }
            if (player.isPlaying()) {
                isPlayingProperty.set(false)
                player.pause()
            } else {
                isPlayingProperty.set(true)
                player.play()
            }
        }
    }

    fun save() {
        recorder?.stop()
    }

    val arcLengthProperty = SimpleDoubleProperty(60.0)
    val animation: Timeline = timeline {
        cycleCount = Timeline.INDEFINITE
        isAutoReverse = true

        keyframe(Duration.millis(750.0)) {
            keyvalue(arcLengthProperty, 100.0, Interpolator.EASE_BOTH)
        }
    }

    fun animate() {
        animation.playFrom(Duration.ZERO)
    }

    fun stop() {
        animation.pause()
    }
}
