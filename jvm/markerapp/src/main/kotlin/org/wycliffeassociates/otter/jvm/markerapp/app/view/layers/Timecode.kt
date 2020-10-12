package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.StrokeType
import javafx.scene.text.Text
import tornadofx.*
import java.lang.Math.floor
import java.util.concurrent.TimeUnit

class Timecode(width: Double, height: Double) : Canvas(width, height) {

    val ctx = graphicsContext2D

    init {
        style {
            backgroundColor += Color.WHITE
        }
    }

    fun drawTimecode(durationMs: Int): Image {
        ctx.fill = Color.WHITE
        ctx.fillRect(0.0, 0.0, width, height)
        ctx.fill = Color.BLACK
        for (i in 0 until width.toInt() step pixelsInSecond(width, durationMs)) {
            ctx.strokeLine(i.toDouble(), floor(height / 2), i.toDouble(), floor(height - 1.0))
            if (i + 10 < width) {
                val text = msToDisplayString(positionToMs((i - 1), width, durationMs))
                ctx.fillText(text, i + 5.0, floor(height - 10.0))
            }
        }
        val img = WritableImage(width.toInt(), height.toInt())
        snapshot(SnapshotParameters(), img)
        return img
    }

    fun pixelsInSecond(width: Double, durationMs: Int): Int {
        val msinPixels = durationMs / width
        return (1000 / msinPixels + 10).toInt()
    }

    fun positionToMs(x: Int, width: Double, durationMs: Int): Int {
        val msinPixels = durationMs / width
        return (x * msinPixels).toInt()
    }

    fun msToDisplayString(ms: Int): String {
        val minute = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val second = TimeUnit.MILLISECONDS.toSeconds(ms.toLong() - TimeUnit.MINUTES.toMillis(minute))

        return String.format("%02d:%02d", minute, second)
    }
}

class TimecodeRegion(durationMs: Int, width: Int, height: Int) : Region() {
    init {
        prefHeightProperty().set(height.toDouble())
        prefWidthProperty().set(width.toDouble())

        style {
            backgroundColor += Color.WHITE
        }

        for (i in 1 until width.toInt() step pixelsInSecond(width.toDouble(), durationMs)) {
            add(Line(i.toDouble(), height / 2.0, i.toDouble(), height - 1.0))
            if (i + 10 < width) {
                val text = msToDisplayString(positionToMs((i - 1), width.toDouble(), durationMs))

                add(Text(i.toDouble(), height - 10.0, text))
            }
        }
    }

    fun pixelsInSecond(width: Double, durationMs: Int): Int {
        val msinPixels = durationMs / width
        return (1000 / msinPixels + 10).toInt()
    }

    fun positionToMs(x: Int, width: Double, durationMs: Int): Int {
        val msinPixels = durationMs / width
        return (x * msinPixels).toInt()
    }

    fun msToDisplayString(ms: Int): String {
        val minute = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val second = TimeUnit.MILLISECONDS.toSeconds(ms.toLong() - TimeUnit.MINUTES.toMillis(minute))

        return String.format("%02d:%02d", minute, second)
    }
}
