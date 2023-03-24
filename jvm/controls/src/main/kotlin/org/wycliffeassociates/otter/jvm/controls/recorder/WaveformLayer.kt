package org.wycliffeassociates.otter.jvm.controls.recorder

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer

private const val USHORT_SIZE = 65535.0

class WaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.stroke = Paint.valueOf("#015AD990")
        context.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array
        var i = buffer.size - 1
        var x = canvas.width

        while (i > 0) {
            val y1 = scaleAmplitude(buffer[i].toDouble(), canvas.height)
            val y2 = scaleAmplitude(buffer[i - 1].toDouble(), canvas.height)

            context.strokeLine(x, y1, x, y2)
            i -= 2
            x -= 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / USHORT_SIZE) + height / 2
    }
}