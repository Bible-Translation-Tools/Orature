package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.text.Font
import javafx.scene.text.FontSmoothingType
import javafx.scene.text.Text
import tornadofx.add
import tornadofx.style
import java.lang.Math.floor

class Timecode(width: Double, height: Double) : Canvas(width, height) {

    val ctx = graphicsContext2D

    init {
        style {
            backgroundColor += Color.WHITE
        }

    }

    fun drawTimecode(): Image {
        ctx.fill = Color.WHITE
        ctx.fillRect(0.0, 0.0, width, height)
        ctx.fill = Color.BLACK
        for(i in 1 until width.toInt() step 500) {
            ctx.strokeLine(i.toDouble()+.5, floor(height / 2), i.toDouble()+.5, floor(height - 1.0))
            if (i + 10 < width) {
                ctx.fillText((i-1).toString(), i + 5.5, floor(height - 10.0))
            }
        }
        val img = WritableImage(width.toInt(), height.toInt())
        snapshot(SnapshotParameters(), img)
        return img
    }
}

class TimecodeRegion(width: Int, height: Int) : Region() {
    init {
        prefHeightProperty().set(height.toDouble())
        prefWidthProperty().set(width.toDouble())

        for(i in 1 until width.toInt() step 500) {
            add(Line(i.toDouble(), height / 2.0, i.toDouble(), height - 1.0))
            if (i + 10 < width) {
                add(Text(i.toDouble(), height - 10.0, i.toString()))
            }
        }
    }
}
