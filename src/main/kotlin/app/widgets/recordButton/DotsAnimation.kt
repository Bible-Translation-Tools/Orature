package app.widgets.recordButton

import app.UIColorsObject.Colors
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class DotsAnimation(var color:String = "#EEEEEE", var fillColor:String= "#CC4141") : HBox() {
    var cir1: Circle
    var cir2: Circle
    var cir3: Circle

    init {
        spacing = 25.0
        alignment = Pos.CENTER
        cir1 = circle {
            radius = 20.0
            fill = c(color)
        }
        cir2 = circle {
            radius = 20.0
            fill = c(color)
        }
        cir3 = circle {
            radius = 20.0
            fill = c(color)
        }
    }

    fun circleCountdown() {
        cir1.fill = c(fillColor)
        cir2.fill = c(fillColor)
        cir3.fill = c(fillColor)

        var timer = Timer()
        // the color is always going to be #0000 because it is transparent, this code makes the dots disappear
        timer.schedule(timerTask { cir3.fill = c("#0000") }, 1000)
        timer.schedule(timerTask { cir2.fill = c("#0000") }, 2000)
        timer.schedule(timerTask { cir1.fill = c("#0000") }, 3000)
    }
}