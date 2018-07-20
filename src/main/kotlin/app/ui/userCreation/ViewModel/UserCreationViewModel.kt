package app.ui.userCreation.ViewModel

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.SimpleBooleanProperty
import java.util.*
import kotlin.concurrent.timerTask

class UserCreationViewModel: AudioInterface {

    var countdownTracker = SimpleStringProperty("")


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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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


    fun changeIcon() {


    }

}