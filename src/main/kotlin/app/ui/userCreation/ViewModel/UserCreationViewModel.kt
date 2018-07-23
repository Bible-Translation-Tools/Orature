package app.ui.userCreation.ViewModel

import app.ui.userCreation.Model.UserCreationModel
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.subjects.BehaviorSubject
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.beans.value.ObservableValue
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class UserCreationViewModel(): ViewModel(), AudioInterface {

    var countdownTracker = SimpleStringProperty("")
    var recordingDone = SimpleBooleanProperty(false)
    val model = UserCreationModel()
    var isRecording = BehaviorSubject.create<Boolean>()
    var doneRecording = BehaviorSubject.create<Boolean>()
    var hasListened = BehaviorSubject.create<Boolean>()


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

        model.audioFile = "recorded Audio"
    }

    fun countdown() {

        Platform.runLater { countdownTracker.set("3") }
        setCountDown("2", 1)         // time in seconds
        setCountDown("1", 2)
        setCountDown("", 3)
    }

    private fun setCountDown(text: String, time: Int){
        var timer = Timer()
        timer.schedule(timerTask { Platform.runLater { countdownTracker.set(text) } }, time.toLong() * 1000 )

    }

    fun recordClicked() {
        model.recordClicked()
        isRecording.onNext(true)
    }

    fun changeIcon() {

        recordingDone.set(true)
    }

    fun reset() {
        model.reset()
        isRecording.onNext(false)
        doneRecording.onNext(false)
        hasListened.onNext(false)

    }

    fun doneRecording() {
        doneRecording.onNext(true)
    }

    fun hasListened() {
        hasListened.onNext(true)
    }

}