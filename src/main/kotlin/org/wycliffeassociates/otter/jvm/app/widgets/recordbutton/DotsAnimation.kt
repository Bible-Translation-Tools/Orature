package org.wycliffeassociates.otter.jvm.app.widgets.recordbutton

import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.shape.Circle
import tornadofx.*
import java.util.*
import kotlin.concurrent.timerTask

class DotsAnimation : HBox() {
    var cir1: Circle
    var cir2: Circle
    var cir3: Circle

    init {
        spacing = 25.0
        alignment = Pos.CENTER
        cir1 = circle {
            radius = 20.0
        }
        cir2 = circle {
            radius = 20.0
        }
        cir3 = circle {
            radius = 20.0
        }
    }

    fun circleCountdown(fillColor: String = "#CC4141", emptyFill: String = "#EDEDED") {
        cir1.fill = c(fillColor)
        cir2.fill = c(fillColor)
        cir3.fill = c(fillColor)

        var timer = Timer()
        // the color is always going to be #0000 because it is transparent, this code makes the dots disappear
        timer.schedule(timerTask { cir3.fill = c(emptyFill) }, 1000)
        timer.schedule(timerTask { cir2.fill = c(emptyFill) }, 2000)
        timer.schedule(timerTask { cir1.fill = c(emptyFill) }, 3000)
    }

    fun resetCircles(emptyFill: String = "#EDEDED") {
        cir1.fill = c(emptyFill)
        cir2.fill = c(emptyFill)
        cir3.fill = c(emptyFill)
    }
}