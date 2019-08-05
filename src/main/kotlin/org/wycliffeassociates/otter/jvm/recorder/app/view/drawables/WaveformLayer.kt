package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer

private const val USHORT_SIZE = 65535.0

class WaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    override fun draw(gc: GraphicsContext, canvas: Canvas) {
        gc.stroke = Color.GRAY
        gc.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array
        var i = 0
        var x = 0.0
        while (i < buffer.size) {
            gc.strokeLine(
                x,
                scaleAmplitude(buffer[i].toDouble(), canvas.height),
                x,
                scaleAmplitude(buffer[i + 1].toDouble(), canvas.height)
            )
            i += 2
            x += 1
        }
    }

    // 16 bit audio range is -32,768 to 32,767, or 65535 (size of unsigned short)
    // This scales the sample to fit within the canvas height, and moves the
    // sample down (-y translate) by half the height
    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height * (sample / USHORT_SIZE) + height / 2
    }
}