package app.ui.widgets.recordButton

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class RecordButtonViewModel: ItemViewModel<Record>() {

    var countdownTracker = SimpleStringProperty("")
    var recordingDone = SimpleBooleanProperty(false)
    val audio = bind(Record::audio)
    val image = bind(Record::image)
    val recording = bind(Record::recording)



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

        recordingDone.set(true)

    }

}