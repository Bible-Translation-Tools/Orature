package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext

interface Drawable {
    fun draw(context: GraphicsContext, canvas: Canvas)
}