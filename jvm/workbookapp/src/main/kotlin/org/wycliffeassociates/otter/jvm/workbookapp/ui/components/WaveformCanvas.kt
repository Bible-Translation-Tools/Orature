package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import tornadofx.View
import tornadofx.hbox
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class WaveformCanvas(width: Double = 150.0, height: Double = 150.0) : View() {

    private var canvasImage: WritableImage = WritableImage(width.toInt(),height.toInt())
    private var canvasImageWriter : PixelWriter = canvasImage.pixelWriter
    private var canvas: Canvas = Canvas(width, height)
    private var canvasGraphicContext : GraphicsContext = canvas.graphicsContext2D
    private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var imageTracker = 1
    private var imageColor : Color = Color.RED

    override val root = hbox {
        children.add(canvas)
    }

    private fun drawWaveform(width: Int, height: Int) {
        imageColor = if(imageTracker % 2 == 0) {
            Color.RED
        } else {
            Color.YELLOW
        }

        imageTracker++

        for (y in (height * .25).toInt() until (height * .75).toInt()) {
            for (x in (width * .25).toInt() until (height * .75).toInt()) {
                canvasImageWriter.setColor(x, y, imageColor)
            }
        }

        canvasGraphicContext.fill = Color.LIGHTGRAY
        canvasGraphicContext.fillRect(0.0, 0.0, canvas.width, canvas.height)
        canvasGraphicContext.drawImage(canvasImage, 0.0, 0.0)
    }

    init {
        executorService.scheduleAtFixedRate({ drawWaveform(width.toInt(), height.toInt()) }, 1, 1, TimeUnit.SECONDS)
    }
}