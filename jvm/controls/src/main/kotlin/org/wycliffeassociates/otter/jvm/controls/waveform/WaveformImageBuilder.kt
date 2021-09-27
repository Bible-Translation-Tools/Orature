/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.waveform

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.jakewharton.rxrelay2.ReplayRelay
import com.sun.glass.ui.Screen
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue

const val SIGNED_SHORT_MAX = 32767

class WaveformImageBuilder(
    private val wavColor: Color = Color.BLACK,
    private val background: Color = Color.TRANSPARENT
) {
    private val logger = LoggerFactory.getLogger(WaveformImageBuilder::class.java)
    private val partialImageWidth = Screen.getMainScreen().platformWidth

    fun build(
        reader: AudioFileReader,
        width: Int = Screen.getMainScreen().platformWidth,
        height: Int = Screen.getMainScreen().platformHeight
    ): Single<Image> {
        return Single
            .fromCallable {
                if (width > 0) {
                    val img = WritableImage(width, height)
                    drawWaveform(img, reader, width, height)
                    img as Image
                } else {
                    WritableImage(1, 1) as Image
                }
            }
            .doOnError { e ->
                logger.error("Error in building WaveformImage", e)
            }
            .subscribeOn(Schedulers.computation())
            .observeOnFx()
    }

    fun buildWaveformAsync(
        reader: AudioFileReader,
        width: Int = Screen.getMainScreen().platformWidth,
        height: Int = Screen.getMainScreen().platformHeight,
        waveform: ReplayRelay<Image>
    ): Completable {
        return Completable
            .fromAction {
                drawPartialImages(reader, width, height, waveform)
            }
            .doOnError { e ->
                logger.error("Error in building WaveformImage", e)
            }
            .subscribeOn(Schedulers.computation())
    }

    private fun drawWaveform(
        img: WritableImage,
        reader: AudioFileReader,
        width: Int,
        height: Int
    ) {
        val framesPerPixel = reader.totalFrames / width

        for (i in 0 until width) {
            val range = computeWaveRange(reader, height, framesPerPixel)

            for (j in 0 until height) {
                img.pixelWriter.setColor(i, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i, j, wavColor)
                }
            }
        }
    }

    private fun drawPartialImages(
        reader: AudioFileReader,
        width: Int,
        height: Int,
        waveform: ReplayRelay<Image>
    ) {
        var img = WritableImage(partialImageWidth, height)
        val framesPerPixel = reader.totalFrames / width
        var counter = 0

        // render fixed-width images until the last one
        val lastImageWidth = width % partialImageWidth
        for (i in 0 until width - lastImageWidth) {
            val range = computeWaveRange(reader, height, framesPerPixel)

            for (j in 0 until height) {
                img.pixelWriter.setColor(i % partialImageWidth, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i % partialImageWidth, j, wavColor)
                }
            }

            counter++
            if (counter == partialImageWidth) {
                counter = 0
                waveform.accept(img)
                img = WritableImage(partialImageWidth, height)
            }
        }

        // render final image with exact width
        if (lastImageWidth != 0) {
            img = WritableImage(
                img.pixelReader,
                0,
                0,
                lastImageWidth,
                height
            )

            for (i in 0 until lastImageWidth) {
                val range = computeWaveRange(reader, height, framesPerPixel)

                for (j in 0 until height) {
                    img.pixelWriter.setColor(i, j, background)
                    if (j in range) {
                        img.pixelWriter.setColor(i, j, wavColor)
                    }
                }
            }
            waveform.accept(img)
        }
    }

    private fun scaleToHeight(value: Int, height: Int): Int {
        return ((value) / (SIGNED_SHORT_MAX * 2).toDouble() * height).toInt()
    }

    private fun computeWaveRange(
        reader: AudioFileReader,
        height: Int,
        framesPerPixel: Int
    ): IntRange {
        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)

        reader.getPcmBuffer(bytes)
        val bb = ByteBuffer.wrap(bytes)
        bb.rewind()
        bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortsArray)

        // translate by half the total range of values (half a short)
        // to push everything below 0; because the image top left is 0,0
        // the absolute value will put the max values close to 0 and the
        // min values further away to the maximum
        val min = ((shortsArray.minOrNull()?.toInt() ?: 0) - SIGNED_SHORT_MAX).absoluteValue
        val max = ((shortsArray.maxOrNull()?.toInt() ?: 0) - SIGNED_SHORT_MAX).absoluteValue

        return scaleToHeight(max, height) until scaleToHeight(min, height)
    }
}
