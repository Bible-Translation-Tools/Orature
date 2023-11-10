package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import com.sun.glass.ui.Screen
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import org.wycliffeassociates.otter.common.domain.narration.AudioScene
import tornadofx.c
import tornadofx.runLater
import java.nio.ByteBuffer

private const val VOLUME_BAR_WIDTH = 25
private const val APP_BAR_WIDTH = 88

// Set up the canvas for the Waveform and Volume bar
class NarrationWaveformRenderer(
    // val renderer: NarrationAudioScene
    val renderer: AudioScene
) {

    val heightProperty = SimpleDoubleProperty(1.0)
    val widthProperty = SimpleDoubleProperty()
    val isRecordingProperty = SimpleBooleanProperty(false)
    val DEFAULT_SCREEN_WIDTH = Screen.getMainScreen().width
    val DEFAULT_SCREEN_HEIGHT = Screen.getMainScreen().height
    val backgroundColor = c("#E5E8EB")
    val waveformColor = c("#66768B")
    val writableImage = WritableImage(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT)
    val pixelWriter = writableImage.pixelWriter
    var pixelFormat: PixelFormat<ByteBuffer> = PixelFormat.getByteRgbInstance()
    private val imageData = ByteArray(DEFAULT_SCREEN_WIDTH * DEFAULT_SCREEN_HEIGHT * 3)

    init {
        fillImageDataWithDefaultColor()
    }

    fun draw(
        context: GraphicsContext,
        canvas: Canvas,
        location: Int,
        reRecordLocation: Int? = null,
        nextVerseLocation: Int? = null
    ): List<IntRange> {
        heightProperty.set(canvas.height)

        //val buffer = renderer.getFrameData()
        val (buffer, viewports) = renderer.getNarrationDrawable(location, reRecordLocation, nextVerseLocation)

        fillImageDataWithDefaultColor()
        addLinesToImageData(buffer)
        drawImageDataToImage()

        runLater {
            context.drawImage(
                writableImage,
                0.0,
                0.0,
                //DEFAULT_SCREEN_WIDTH.toDouble(),
                canvas.width + ((APP_BAR_WIDTH + VOLUME_BAR_WIDTH) * (canvas.width / DEFAULT_SCREEN_WIDTH.toDouble())),
                DEFAULT_SCREEN_HEIGHT.toDouble()
            )

            context.stroke = javafx.scene.paint.Color.RED
            context.lineWidth = 2.0
            context.strokeLine(canvas.width / 2.0, 0.0, canvas.width / 2.0, canvas.height)
        }

        return viewports
    }

    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }

    fun fillImageDataWithDefaultColor() {
        var i = 0
        for (y in 0 until DEFAULT_SCREEN_HEIGHT) {
            for (x in 0 until DEFAULT_SCREEN_WIDTH) {
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
        pixelWriter.setPixels(
            0,
            0,
            DEFAULT_SCREEN_WIDTH,
            DEFAULT_SCREEN_HEIGHT,
            pixelFormat,
            imageData,
            0,
            DEFAULT_SCREEN_WIDTH * 3
        )
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

    fun clearActiveRecordingData() {
        renderer.clear()
    }

    fun close() {
        renderer.close()
    }
}