package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

class BaseWaveLine : Drawable {
    override fun draw(context: GraphicsContext, canvas: Canvas) {
        context.fill = Color.CYAN
        context.stroke = Color.CYAN
        context.lineWidth = 1.5
        context.strokeLine(0.0, canvas.height / 2.0, canvas.width, canvas.height / 2.0)
    }
}