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

import java.io.File
import javafx.animation.Interpolator
import javafx.animation.Timeline
import javafx.application.Platform
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.util.Duration
import javax.inject.Inject
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.wav.WavFile
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

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    var wav: AudioFile? = null

    var player: IAudioPlayer? = null
    var recorder: IAudioRecorder? = null
    var writer: WavFileWriter? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDock() {
        wav = AudioFile(directoryProvider.createTempFile("verbalize",".wav").apply { deleteOnExit() }, 1, 44100, 16)
        recorder = audioConnectionFactory.getRecorder()
        player = audioConnectionFactory.getPlayer()
        prepareRecorder()
    }

    val isPlayingProperty = SimpleBooleanProperty(false)
    var isLoaded = false

    val hasContentProperty = SimpleBooleanProperty(false)

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty

    var hasWrittenProperty = SimpleBooleanProperty(false)
    var hasWritten by hasWrittenProperty

    val canSaveProperty: BooleanBinding = (recordingProperty.not()).and(hasWrittenProperty)

    fun toggle() {
        if (isRecording) {
            stop()
            recorder?.stop()
            writer?.pause()
            hasWritten = true
        } else {
            animate()
            recorder?.start()
            writer?.start()
        }
        isRecording = !isRecording
    }

    fun reRec() {
        hasContentProperty.set(false)
        if (isLoaded && player?.isPlaying() == true) {
            isPlayingProperty.set(false)
            player?.pause()
            player?.close()
        }
        isLoaded = false
        wav?.file?.delete()
        wav = AudioFile(File.createTempFile("temp",".wav"), 1, 44100, 16)
        toggle()
    }

    fun playToggle() {
        if (!isLoaded) {
            wav?.let { wav ->
                player?.load(wav.file)
                player?.addEventListener {
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
        if(player?.isPlaying() == true) {
            isPlayingProperty.set(false)
            player?.pause()
        } else {
            isPlayingProperty.set(true)
            player?.play()
        }
    }

    fun save() {
        recorder?.stop()
    }

    val arcLengthProperty = SimpleDoubleProperty(60.0)
    var animation: Timeline = timeline {
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
        animation?.pause()
    }

    private fun prepareRecorder() {
        recorder?.let { recorder ->
            writer = WavFileWriter(wav!!, recorder.getAudioStream()) {
                Platform.runLater {
                    hasContentProperty.set(true)
                }
            }
        }
    }
}
