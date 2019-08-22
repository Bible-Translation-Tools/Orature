package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.animation.AnimationTimer
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.paint.Paint

class FramerateView : Label() {

    private val frameTimes = LongArray(100)
    private var frameTimeIndex = 0
    private var arrayFilled = false

    private val builder = StringBuilder("FPS: 000")

    //code from:
    //https://stackoverflow.com/questions/28287398/what-is-the-preferred-way-of-getting-the-frame-rate-of-a-javafx-application
    val at = object : AnimationTimer() {

        override fun handle(currentNanoTime: Long) {
            val oldFrameTime = frameTimes[frameTimeIndex]
            frameTimes[frameTimeIndex] = currentNanoTime
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.size
            if (frameTimeIndex == 0) {
                arrayFilled = true
            }
            if (arrayFilled) {
                val elapsedNanos = currentNanoTime - oldFrameTime
                val elapsedNanosPerFrame = elapsedNanos / frameTimes.size
                val frameRate = 1_000_000_000 / elapsedNanosPerFrame
                builder.replace(5, 8, frameRate.toString())
                text = builder.toString()
            }
        }
    }.start()

    init {
        prefHeight = 50.0
        prefWidth = 100.0
        alignment = Pos.TOP_LEFT
        textFill = Paint.valueOf("#00FF00")
    }
}