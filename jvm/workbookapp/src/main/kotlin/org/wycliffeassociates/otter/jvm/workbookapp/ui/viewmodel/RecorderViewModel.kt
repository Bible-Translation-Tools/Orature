/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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

import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Single
import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.Recordable
import org.wycliffeassociates.otter.common.domain.content.TakeCreator
import org.wycliffeassociates.otter.common.domain.content.WorkbookFileNamerBuilder
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.RecordingTimer
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.controls.bar.VolumeBar
import org.wycliffeassociates.otter.jvm.controls.canvas.BaseWaveLine
import org.wycliffeassociates.otter.jvm.controls.canvas.CanvasFragment
import org.wycliffeassociates.otter.jvm.controls.canvas.WaveformLayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import tornadofx.ViewModel
import tornadofx.getValue
import tornadofx.setValue
import java.io.File
import javax.inject.Inject

class RecorderViewModel : ViewModel() {

    val settingsViewModel: SettingsViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    @Inject lateinit var takeCreator: TakeCreator

    enum class Result {
        SUCCESS,
        CANCELLED
    }

    @Inject
    lateinit var audioConnectionFactory: AudioConnectionFactory

    val targetFileProperty = SimpleObjectProperty<File>(null)
    var hasWrittenProperty = SimpleBooleanProperty(false)

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty
    lateinit var recorder: IAudioRecorder

    /**
     * These property must be assigned everytime the view is docked, since it could be dirty
     * from the other View(s) that share this ViewModel.
     * */
    lateinit var waveformCanvas: CanvasFragment
    lateinit var volumeCanvas: CanvasFragment

    val timerTextProperty = SimpleStringProperty("00:00:00")
    lateinit var tempTake: File
    lateinit var wavAudio: OratureAudioFile
    lateinit var writer: WavFileWriter

    private val timer = RecordingTimer()
    private lateinit var renderer: ActiveRecordingRenderer

    val at = object : AnimationTimer() {
        override fun handle(now: Long) {
            waveformCanvas.draw()
            volumeCanvas.draw()
            val t = timer.timeElapsed
            timerTextProperty.value = String.format(
                "%02d:%02d:%02d",
                t / 3600000,
                (t / 60000) % 60,
                (t / 1000) % 60
            )
        }
    }

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onViewReady(width: Int) {
        initializeAudio()
        val renderedWidth = width - volumeCanvas.minWidth.toInt()
        renderer = ActiveRecordingRenderer(
            recorder.getAudioStream(),
            writer.isWriting,
            renderedWidth,
            secondsOnScreen = 10
        )

        volumeCanvas.addDrawable(VolumeBar(recorder.getAudioStream()))
        val waveformLayer = WaveformLayer(renderer, settingsViewModel.appColorMode.toObservable())
        waveformCanvas.addDrawable(BaseWaveLine())
        waveformCanvas.addDrawable(waveformLayer)

        at.start()
        recorder.start()
    }

    fun toggle() {
        if (isRecording) {
            hasWrittenProperty.value = true
            pause()
        } else {
            startRecording()
        }
        isRecording = !isRecording
    }

    private fun startRecording() {
        writer.start()
        timer.start()
    }

    private fun pause() {
        writer.pause()
        timer.pause()
    }

    fun saveAndQuit(): Result {
        pause()
        at.stop()
        recorder.stop()
        waveformCanvas.clearDrawables()
        return if (hasWrittenProperty.value) {
            targetFileProperty.value?.let {
                wavAudio.file.copyTo(it, true)
            }
            targetFileProperty.set(null)
            reset()
            Result.SUCCESS
        } else {
            reset()
            Result.CANCELLED
        }
    }

    fun cancel() {
        pause()
        isRecording = false
        at.stop()
        recorder.stop()
        waveformCanvas.clearDrawables()
        reset()
    }

    fun reset() {
        writer.pause()
        writer.writer.dispose()
        timer.pause()
        timer.reset()
        hasWrittenProperty.value = false

        // clear waveform
        renderer.clearData()
        renderer.setRecordingStatusObservable(writer.isWriting)
    }

    fun createTake(recordable: Recordable, chunk: Chunk?, createEmpty: Boolean): Single<Take> {
        val namer = WorkbookFileNamerBuilder.createFileNamer(
            workbook = workbookDataStore.workbook,
            chapter = workbookDataStore.chapter,
            chunk = chunk,
            recordable = recordable,
            rcSlug = workbookDataStore.workbook.sourceMetadataSlug
        )
        val chapterAudioDir = workbookDataStore.workbook.projectFilesAccessor.audioDir
            .resolve(namer.formatChapterNumber())
            .apply { mkdirs() }

        return recordable.audio.getNewTakeNumber()
            .map { takeNumber ->
                takeCreator.createNewTake(
                    takeNumber,
                    namer.generateName(takeNumber, AudioFileFormat.WAV),
                    chapterAudioDir,
                    createEmpty
                )
            }
    }

    private fun initializeAudio() {
        tempTake = createTempRecordingTake()
        wavAudio = OratureAudioFile(tempTake, 1, 44100, 16)
        recorder = audioConnectionFactory.getRecorder()
        writer = WavFileWriter(wavAudio, recorder.getAudioStream()) { /* no op */ }
    }

    private fun createTempRecordingTake(): File {
        return kotlin.io.path.createTempFile("otter-take", ".wav").toFile()
            .also {
                it.deleteOnExit()
                targetFileProperty.value?.copyTo(it, true)
            }
    }
}