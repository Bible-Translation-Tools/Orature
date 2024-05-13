/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.waveform

import io.reactivex.Observable
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelWriter
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.common.domain.narration.AudioScene
import org.wycliffeassociates.otter.common.domain.theme.AppTheme
import tornadofx.c
import tornadofx.runLater
import java.nio.ByteBuffer

// Set up the canvas for the Waveform and Volume bar
class NarrationWaveformRenderer(
    private val renderer: AudioScene,
    val renderWidth: Int,
    val renderHeight: Int,
    val colorThemeObservable: Observable<ColorTheme>,
) {
    private var backgroundColor: Color = c("#E5E8EB")
    private var waveformColor: Color = c("#66768B")
    private val writableImage = WritableImage(renderWidth, renderHeight)
    var pixelFormat: PixelFormat<ByteBuffer> = PixelFormat.getByteRgbInstance()
    private val imageData = ByteArray(renderWidth * renderHeight * 3)

    init {
        fillImageDataWithDefaultColor()
        colorThemeObservable.subscribe {
            it?.let {
                updateWaveformColors(it)
            }
        }
    }

    fun updateWaveformColors(theme: ColorTheme) {
        if (theme == ColorTheme.LIGHT) {
            backgroundColor = c("#E5E8EB")
            waveformColor = c("#66768B")
        } else {
            backgroundColor = c("#343434")
            waveformColor = c("#808080")
        }
    }

    fun draw(
        context: GraphicsContext,
        canvas: Canvas,
        location: Int,
        reRecordLocation: Int? = null,
        nextVerseLocation: Int? = null
    ): List<IntRange> {
        val (_, viewports) = generateImage(
            location,
            canvas.height,
            writableImage,
            reRecordLocation,
            nextVerseLocation
        )

        runLater {
            // due to the renderer (PCM compressor, etc) being initialized with the screen width, don't scale the image
            // however, this means the image needs to be translated by the half the delta between the screen and canvas
            context.drawImage(
                writableImage,
                (canvas.width - renderWidth) / 2.0,
                0.0
            )

            context.stroke = Color.RED
            context.lineWidth = 2.0
            context.strokeLine(canvas.width / 2.0, 0.0, canvas.width / 2.0, canvas.height)
        }

        return viewports
    }

    class RenderedFrame(val image: Image, val viewports: List<IntRange>) {
        operator fun component1(): Image = image
        operator fun component2(): List<IntRange> = viewports
    }

    fun generateImage(
        location: Int,
        canvasHeight: Double,
        writableImage: WritableImage,
        reRecordLocation: Int?,
        nextVerseLocation: Int?,
    ): RenderedFrame {
        val (buffer, viewports) = renderer.getNarrationDrawable(location, reRecordLocation, nextVerseLocation)

        fillImageDataWithDefaultColor()
        addLinesToImageData(buffer, canvasHeight)
        drawImageDataToImage(writableImage.pixelWriter)

        return RenderedFrame(writableImage, viewports)
    }

    private fun scaleAmplitude(sample: Double, height: Double): Double {
        return height / (Short.MAX_VALUE * 2) * (sample + Short.MAX_VALUE)
    }

    private fun fillImageDataWithDefaultColor() {
        var i = 0
        for (y in 0 until renderHeight) {
            for (x in 0 until renderWidth) {
                imageData[i] = (backgroundColor.red * 255).toInt().toByte()
                imageData[i + 1] = (backgroundColor.green * 255).toInt().toByte()
                imageData[i + 2] = (backgroundColor.blue * 255).toInt().toByte()
                i += 3
            }
        }
    }

    private fun addLinesToImageData(
        buffer: FloatArray,
        canvasHeight: Double
    ) {
        for (x in 0 until buffer.size / 2) {
            val y1 = scaleAmplitude(buffer[x * 2].toDouble(), canvasHeight)
            val y2 = scaleAmplitude(buffer[x * 2 + 1].toDouble(), canvasHeight)

            for (y in minOf(y1.toInt(), y2.toInt())..maxOf(y1.toInt(), y2.toInt())) {
                imageData[(x + y * renderWidth) * 3] = (waveformColor.red * 255).toInt().toByte()
                imageData[(x + y * renderWidth) * 3 + 1] = (waveformColor.green * 255).toInt().toByte()
                imageData[(x + y * renderWidth) * 3 + 2] = (waveformColor.blue * 255).toInt().toByte()
            }
        }
    }

    private fun drawImageDataToImage(pixelWriter: PixelWriter) {
        pixelWriter.setPixels(
            0,
            0,
            renderWidth,
            renderHeight,
            pixelFormat,
            imageData,
            0,
            renderWidth * 3
        )
    }

    fun clearActiveRecordingData() {
        renderer.clear()
    }

    fun close() {
        renderer.close()
    }
}