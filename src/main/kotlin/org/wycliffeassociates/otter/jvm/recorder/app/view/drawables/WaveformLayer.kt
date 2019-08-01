package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.recorder.ActiveRecordingRenderer

class WaveformLayer(private val renderer: ActiveRecordingRenderer) : Drawable {

    override fun draw(gc: GraphicsContext, canvas: Canvas) {
        gc.stroke = Color.GRAY
        gc.lineWidth = 1.0

        val buffer = renderer.floatBuffer.array

        //for (i in 0 until buffer.size step 4) {
        var i = 0
        while (i < buffer.size) {
            gc.strokeLine(
                buffer[i].toDouble(),
                scaleAmplitude(buffer[i + 1].toDouble(), canvas.height),
                buffer[i + 2].toDouble(),
                scaleAmplitude(buffer[i + 3].toDouble(), canvas.height)
            )
            i += 4
        }
    }

    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return sample * (height / 65535.0) + height / 2
    }
}