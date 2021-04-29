package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.chunking

import java.io.File
import javafx.animation.AnimationTimer
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer
import org.wycliffeassociates.otter.common.recorder.RecordingTimer
import org.wycliffeassociates.otter.common.recorder.WavFileWriter
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioRecorder
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.ViewModel
import tornadofx.add
import tornadofx.getValue
import tornadofx.setValue

class VerbalizeViewModel : ViewModel() {

    var wav = WavFile(File.createTempFile("temp",".wav").apply { deleteOnExit() }, 1, 44100, 16)
    var recorder = AudioRecorder()
    var player = AudioBufferPlayer()

    var writer = WavFileWriter(wav, recorder.getAudioStream()) {
        println("closed")
    }

    @Volatile
    var recordingProperty = SimpleBooleanProperty(false)
    var isRecording by recordingProperty

    var hasWrittenProperty = SimpleBooleanProperty(false)
    var hasWritten by hasWrittenProperty

    var canSaveProperty: BooleanBinding = (recordingProperty.not()).and(hasWrittenProperty)

    fun toggle() {
        if (isRecording) {
            recorder.stop()
            writer.pause()
            hasWritten = true
        } else {
            recorder.start()
            writer.start()
        }
        isRecording = !isRecording
    }

    fun reRec() {
        wav.file.delete()
        wav = WavFile(File.createTempFile("temp",".wav"), 1, 44100, 16)
        writer = WavFileWriter(wav, recorder.getAudioStream()) { }
        toggle()
    }

    fun playToggle() {
        // if(!player.isPlaying()) {
            //player.stop()
           // player.close()
        println(wav.file.absolutePath)
        println(wav.file.length())
            player.load(wav.file)
            player.play()
//        } else {
//            player.pause()
//        }
    }

    fun save() {
        recorder.stop()
    }
}
