package app.ui.userCreation.ViewModel

import io.reactivex.subjects.PublishSubject
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty

import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class UserCreationViewModel() : ViewModel(), AudioInterface {

    var countdownTracker = SimpleStringProperty("")
    var recordingDone = SimpleBooleanProperty(false)
    var isRecording = PublishSubject.create<Boolean>()
    var doneRecording = PublishSubject.create<Boolean>()
    var hasListened = PublishSubject.create<Boolean>()


    override fun getAudio() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadAudio() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pauseAudio() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playAudio() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun recordAudio() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun countdown() {

        Platform.runLater { countdownTracker.set("3") }
        setCountDown("2", 1)         // time in seconds
        setCountDown("1", 2)
        setCountDown("", 3)
    }

    private fun setCountDown(text: String, seconds: Int) {
        var timer = Timer()
        timer.schedule(timerTask { Platform.runLater { countdownTracker.set(text) } }, seconds.toLong() * 1000)

    }

    fun recordClicked() {
        isRecording.onNext(true)
    }

    fun changeIcon() {
        recordingDone.set(true)
    }

    fun reset() {
        isRecording.onNext(false)
        doneRecording.onNext(false)
        hasListened.onNext(false)
    }

    fun doneRecording() {
        doneRecording.onNext(true)
    }
}