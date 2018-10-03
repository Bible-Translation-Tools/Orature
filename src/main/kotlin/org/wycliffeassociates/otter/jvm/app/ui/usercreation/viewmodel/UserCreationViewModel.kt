package org.wycliffeassociates.otter.jvm.app.ui.usercreation.viewmodel

import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty

import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class UserCreationViewModel() : ViewModel() {

    var countdownTracker = SimpleStringProperty("")
    var recordingDone = SimpleBooleanProperty(false)
    var isRecording = SimpleBooleanProperty(false)
    var doneRecording = PublishSubject.create<Boolean>()
    var hasListened = PublishSubject.create<Boolean>()
    var timer = Timer()


    fun countdown() {
        timer = Timer()
        Platform.runLater { countdownTracker.set("3") }
        setCountDown("2", 1)         // time in seconds
        setCountDown("1", 2)
        setCountDown("", 3)
    }

    private fun setCountDown(text: String, seconds: Int) {
        timer.schedule(timerTask { Platform.runLater { countdownTracker.set(text) } }, seconds.toLong() * 1000)
    }

    fun stopCountdown() {
        countdownTracker.set("")
        timer.cancel()
        timer.purge()
    }

    fun recordClicked() {
        isRecording.set(true)
    }

    fun changeIcon() {
        recordingDone.set(true)
    }

    fun reset() {
        isRecording.set(false)
        doneRecording.onNext(false)
        hasListened.onNext(false)
    }

    fun doneRecording() {
        doneRecording.onNext(true)
    }
}