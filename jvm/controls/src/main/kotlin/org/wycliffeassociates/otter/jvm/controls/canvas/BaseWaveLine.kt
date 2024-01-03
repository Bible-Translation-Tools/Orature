package org.wycliffeassociates.otter.jvm.controls.canvas

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint

class BaseWaveLine : IDrawable {
    override fun draw(
        context: GraphicsContext,
        canvas: Canvas,
    ) {
        context.fill = Paint.valueOf("#1A1A1A")
        context.stroke = Paint.valueOf("#1A1A1A")
        context.lineWidth = 1.0
        context.strokeLine(0.0, canvas.height / 2.0, canvas.width, canvas.height / 2.0)
    }
}
