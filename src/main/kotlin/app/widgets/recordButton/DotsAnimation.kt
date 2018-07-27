package app.widgets.recordButton

import app.MyApp.Companion.Colors
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class DotsAnimation : HBox() {


    lateinit var cir1: Circle
    lateinit var cir2: Circle
    lateinit var cir3: Circle

    init {
        spacing = 25.0
        alignment = Pos.CENTER
        cir1 = circle {
            //hide()
            centerX = 10.0
            centerY = 20.0
            radius = 20.0
            fill = c(Colors["baseBackground"])
        }

        cir2 = circle {
            //hide()

            isVisible = true
            centerX = 100.0
            centerY = 20.0
            radius = 20.0
            fill = c(Colors["baseBackground"])
        }

        cir3 = circle {
            // hide()
            isVisible = true
            centerX = 200.0
            centerY = 20.0
            radius = 20.0
            fill = c(Colors["baseBackground"])
        }
    }

    fun circleCountdown() {
        cir1.fill = c(Colors["primary"])
        cir2.fill = c(Colors["primary"])
        cir3.fill = c(Colors["primary"])

        var timer = Timer()
        timer.schedule(timerTask { cir3.fill = c(Colors["baseBackground"])}, 1000)
        timer.schedule(timerTask { cir2.fill = c(Colors["baseBackground"]) }, 2000)
        timer.schedule(timerTask { cir1.fill = c(Colors["baseBackground"]) }, 3000)
    }

    fun invisible() {
        cir1.fill = c(Colors["base"])
        cir2.fill = c(Colors["base"])
        cir3.fill = c(Colors["base"])
    }

    fun resetCircles() {
        cir1.fill = c(Colors["baseBackground"])
        cir2.fill = c(Colors["baseBackground"])
        cir3.fill = c(Colors["baseBackground"])
    }
}