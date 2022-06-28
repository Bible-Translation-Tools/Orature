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
package org.wycliffeassociates.otter.jvm.recorder.app.viewmodel

import javafx.animation.AnimationTimer
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.RecordingTimer
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.BaseWaveLine
import org.wycliffeassociates.otter.jvm.recorder.app.view.CanvasFragment
import org.wycliffeassociates.otter.jvm.recorder.app.view.FramerateView
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.WaveformLayer
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.VolumeBar
import tornadofx.ViewModel
import tornadofx.add
import tornadofx.getValue
import tornadofx.setValue
import java.io.File
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import tornadofx.runLater

class RecorderViewModel : ViewModel() {

    val parameters = (scope as ParameterizedScope).parameters
    val targetFile = File(parameters.named["wav"])

    lateinit var tempTake: File
    lateinit var wavAudio: AudioFile
    lateinit var writer: WavFileWriter

    private val logger = LoggerFactory.getLogger(this.javaClass)

    val recorder = (scope.workspace.params["audioConnectionFactory"] as AudioConnectionFactory).getRecorder()

    val waveformView = CanvasFragment()
    val volumeBarView = CanvasFragment()

    val fps = FramerateView()

    val volumeBar = VolumeBar(recorder.getAudioStream())

    val timer = RecordingTimer()
    val timerTextProperty = SimpleStringProperty("00:00:00")
    var timerText by timerTextProperty

    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            waveformView.draw()
            volumeBarView.draw()
            val t = timer.timeElapsed
            timerText = String.format(
                "%02d:%02d:%02d",
                t / 3600000,
                (t / 60000) % 60,
                (t / 1000) % 60
            )
        }
    }

    init {
        initializeAudioData()
        volumeBarView.addDrawable(volumeBar)
        waveformView.addDrawable(BaseWaveLine())
        if (app.parameters.named.containsKey("debug")) {
            waveformView.add(fps)
        }
    }
    private lateinit var renderer: ActiveRecordingRenderer

    fun onViewReady(width: Int) {
        renderer = ActiveRecordingRenderer(
            recorder.getAudioStream(),
            writer.isWriting,
            width,
            secondsOnScreen = 10
        )
        val waveformLayer = WaveformLayer(renderer)
        waveformView.addDrawable(waveformLayer)

        at.start()
        recorder.start()
    }

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty

    var hasWrittenProperty = SimpleBooleanProperty(false)
    var hasWritten by hasWrittenProperty

    var canSaveProperty: BooleanBinding = (recordingProperty.not()).and(hasWrittenProperty)

    fun toggle() {
        if (isRecording) {
            writer.pause()
            hasWritten = true
            timer.pause()
        } else {
            writer.start()
            timer.start()
        }
        isRecording = !isRecording
    }

    fun save() {
        at.stop()
        recorder.stop()
        writer.writer.dispose()
        wavAudio.file.copyTo(targetFile, true)

        logger.info("Closing Recorder...")
        runLater {
            (scope as ParameterizedScope).navigateBack()
        }
    }

    fun reset() {
        writer.pause()
        writer.writer.dispose()
        timer.pause()
        timer.reset()
        hasWritten = false

        // reset take
        initializeAudioData()

        // clear waveform
        renderer.clearData()
        renderer.setRecordingStatusObservable(writer.isWriting)
    }

    private fun initializeAudioData() {
        tempTake = createTempRecordingTake()
        wavAudio = AudioFile(tempTake, 1, 44100, 16)
        writer = WavFileWriter(wavAudio, recorder.getAudioStream()) { /* no op */ }
    }

    private fun createTempRecordingTake(): File {
        return kotlin.io.path.createTempFile("otter-take",".wav").toFile()
            .also {
                it.deleteOnExit()
                targetFile.copyTo(it, true)
            }
    }
}
