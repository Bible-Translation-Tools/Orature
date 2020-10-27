package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsInSecond
import org.wycliffeassociates.otter.jvm.markerapp.app.view.positionToMs
import java.lang.Math.floor
import java.util.concurrent.TimeUnit

class Timecode(width: Double, height: Double) : Canvas(width, height) {

    val ctx = graphicsContext2D

    init {
        styleClass.add("vm-timecode")
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

    fun msToDisplayString(ms: Int): String {
        val minute = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val second = TimeUnit.MILLISECONDS.toSeconds(ms.toLong() - TimeUnit.MINUTES.toMillis(minute))

        return String.format("%02d:%02d", minute, second)
    }
}
