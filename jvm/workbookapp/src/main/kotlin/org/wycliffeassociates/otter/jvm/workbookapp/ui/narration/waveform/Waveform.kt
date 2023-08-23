package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import org.wycliffeassociates.otter.jvm.controls.waveform.Drawable
import tornadofx.c
import java.lang.Math.abs
import java.nio.ByteBuffer


// Set up the canvas for the Waveform and Volume bar
class Waveform(
    val renderer : ExistingAndIncomingAudioRenderer
) : Drawable {

    val heightProperty = SimpleDoubleProperty(1.0)
    val widthProperty = SimpleDoubleProperty()
    val isRecordingProperty = SimpleBooleanProperty(false)
    val DEFAULT_SCREEN_WIDTH = 1920
    val DEFAULT_SCREEN_HEIGHT = 1080
    val backgroundColor = c("#E5E8EB")
    val waveformColor = c("#66768B")
    val writableImage = WritableImage(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT)
    val pixelWriter = writableImage.pixelWriter
    var pixelFormat: PixelFormat<ByteBuffer> = PixelFormat.getByteRgbInstance()
    private val imageData = ByteArray(DEFAULT_SCREEN_WIDTH * DEFAULT_SCREEN_HEIGHT * 3)

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        val buffer = renderer.floatBuffer.array

        fillImageDataWithDefaultColor()
        addLinesToImageData(buffer)
        drawImageDataToImage()

        context.drawImage(writableImage, (0.0 + minOf(widthProperty.value - DEFAULT_SCREEN_WIDTH, 0.0)), 0.0, DEFAULT_SCREEN_WIDTH.toDouble(), DEFAULT_SCREEN_HEIGHT.toDouble())
    }

    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }

    fun fillImageDataWithDefaultColor() {
        var i = 0
        for (y in 0 until DEFAULT_SCREEN_HEIGHT) {
            for ( x in 0 until DEFAULT_SCREEN_WIDTH) {
                imageData[i] = (backgroundColor.red * 255).toInt().toByte()
                imageData[i + 1] = (backgroundColor.green * 255).toInt().toByte()
                imageData[i + 2] = (backgroundColor.blue * 255).toInt().toByte()
                i += 3
            }
        }
    }

    fun addLinesToImageData(buffer: FloatArray) {
        for (x in 0 until buffer.size / 2) {
            val y1 = scaleAmplitude(buffer[x * 2].toDouble(), heightProperty.value)
            val y2 = scaleAmplitude(buffer[x * 2 + 1].toDouble(), heightProperty.value)

            for (y in minOf(y1.toInt(), y2.toInt())..maxOf(y1.toInt(), y2.toInt())) {
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3] = (waveformColor.red * 255).toInt().toByte()
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3 + 1] = (waveformColor.green * 255).toInt().toByte()
                imageData[(x + y * DEFAULT_SCREEN_WIDTH) * 3 + 2] = (waveformColor.blue * 255).toInt().toByte()
            }
        }
    }

    fun drawImageDataToImage() {
        pixelWriter.setPixels(0, 0, DEFAULT_SCREEN_WIDTH,
            DEFAULT_SCREEN_HEIGHT, pixelFormat, imageData,
            0, DEFAULT_SCREEN_WIDTH * 3)
    }

    fun drawImageToCanvas(context: GraphicsContext, canvas: Canvas) {
        var startingXPosition = (0.0 + minOf(widthProperty.value - DEFAULT_SCREEN_WIDTH, 0.0))
        context.drawImage(
            writableImage,
            startingXPosition,
            0.0,
            DEFAULT_SCREEN_WIDTH.toDouble(),
            DEFAULT_SCREEN_HEIGHT.toDouble()
        )
    }

    init {
        fillImageDataWithDefaultColor()
    }

}