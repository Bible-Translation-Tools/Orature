package org.wycliffeassociates.otter.jvm.controls.canvas

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext

interface IDrawable {
    fun draw(context: GraphicsContext, canvas: Canvas)
}
