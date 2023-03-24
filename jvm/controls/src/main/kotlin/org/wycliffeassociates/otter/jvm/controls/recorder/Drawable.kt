package org.wycliffeassociates.otter.jvm.controls.recorder

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext

interface Drawable {
    fun draw(context: GraphicsContext, canvas: Canvas)
}