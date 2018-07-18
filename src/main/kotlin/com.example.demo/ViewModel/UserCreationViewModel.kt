package com.example.demo.ViewModel

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ObservableStringValue
import sun.jvm.hotspot.runtime.Thread
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

        var timer = Timer()

        Platform.runLater(Runnable() {
            run {
                countdownTracker.set("3")
            }
        })

        timer.schedule(timerTask { Platform.runLater(Runnable() {
            run {
                countdownTracker.set("2")
            }
        })
        }, 1000 )
        timer.schedule(timerTask { Platform.runLater(Runnable() {
            run {
                countdownTracker.set("1")
            }
        })
        }, 2000 )
        timer.schedule(timerTask { Platform.runLater(Runnable() {
            run {
                countdownTracker.set("")
            }
        })
        }, 3000 )

    }

    fun changeIcon() {

    }

}