package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Pos
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.Drawable
import tornadofx.*

class CanvasFragment : StackPane() {

    private val drawables = arrayListOf<Drawable>()

    private val cvs = ResizableCanvas()
    private val ctx = cvs.graphicsContext2D

    init {
        addClass("waveform")
        alignment = Pos.TOP_LEFT

        add(cvs)

        cvs.widthProperty().addListener { _ -> draw() }
        cvs.heightProperty().addListener { _ -> draw() }

        draw()
    }

    fun addDrawable(drawable: Drawable) {
        drawables.add(drawable)
    }

    fun draw() {
        ctx.clearRect(0.0, 0.0, cvs.width, cvs.height)
        var i = 0
        while (i < drawables.size) {
            drawables[i].draw(ctx, cvs)
            i++
        }
    }
}
