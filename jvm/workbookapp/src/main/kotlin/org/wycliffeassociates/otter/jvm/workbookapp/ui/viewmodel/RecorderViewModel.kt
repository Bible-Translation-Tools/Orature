package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import javafx.animation.AnimationTimer
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
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
        val waveformLayer = WaveformLayer(renderer)
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

    private fun initializeAudio() {
        tempTake = createTempRecordingTake()
        wavAudio = OratureAudioFile(tempTake, 1, 44100, 16)
        recorder = audioConnectionFactory.getRecorder()
        writer = WavFileWriter(wavAudio, recorder.getAudioStream()) { /* no op */ }
    }

    private fun createTempRecordingTake(): File {
        return kotlin.io.path.createTempFile("otter-take",".wav").toFile()
            .also {
                it.deleteOnExit()
                targetFileProperty.value?.copyTo(it, true)
            }
    }
}