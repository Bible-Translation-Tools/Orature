package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Paint
import org.wycliffeassociates.otter.jvm.recorder.app.view.drawables.Drawable
import tornadofx.add
import tornadofx.canvas
import tornadofx.hgrow
import tornadofx.vgrow

class CanvasFragment(color: String = "#000000") : StackPane() {

    private val drawables = arrayListOf<Drawable>()

    private val cvs = canvas {
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
    }
    private val ctx = cvs.graphicsContext2D

    init {
        background = Background(BackgroundFill(Paint.valueOf(color), CornerRadii.EMPTY, Insets.EMPTY))
        alignment = Pos.TOP_LEFT

        add(cvs)

        cvs.widthProperty().bind(this.widthProperty())
        cvs.heightProperty().bind(this.heightProperty())

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