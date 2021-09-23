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
import com.sun.glass.ui.Screen
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
import kotlin.math.max

const val SIGNED_SHORT_MAX = 32767

class WaveformImageBuilder(
    private val wavColor: Color = Color.BLACK,
    private val background: Color = Color.TRANSPARENT,
    private val paddingColor: Color = background
) {
    private val logger = LoggerFactory.getLogger(WaveformImageBuilder::class.java)
    private val PARTIAL_IMAGE_WIDTH = Screen.getMainScreen().platformWidth

    private data class MinMax(var min: Int, var max: Int)

    fun build(
        reader: AudioFileReader,
        padding: Int = 0,
        fitToAudioMax: Boolean = true,
        width: Int = Screen.getMainScreen().platformWidth,
        height: Int = Screen.getMainScreen().platformHeight
    ): Single<Image> {
        return Single
            .fromCallable {
                if (width > 0) {
                    val img = WritableImage(width + (2 * padding), height)
                    val (globalMin, globalMax) = drawWaveform(img, reader, width, height, padding)
                    val newHeight = globalMax - globalMin
                    if (fitToAudioMax) {
                        val image2 = WritableImage(
                            img.pixelReader,
                            0,
                            globalMin - newHeight,
                            width + (padding * 2),
                            (newHeight) * 2
                        )
                        image2 as Image
                    } else img as Image
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

    fun buildPartialImages(
        reader: AudioFileReader,
        padding: Int = 0,
        fitToAudioMax: Boolean = true,
        width: Int = Screen.getMainScreen().platformWidth,
        height: Int = Screen.getMainScreen().platformHeight
    ): Single<List<Image>> {
//        return drawImages(reader, width, height, padding, MinMax(0,0))

        return Single
            .fromCallable {
                val minMax = MinMax(0, 1)
                val images = drawImages(reader, width, height, padding, minMax)
                if (fitToAudioMax) {
                    val newHeight = minMax.max - minMax.min
                    images.map {
                        WritableImage(
                            it.pixelReader,
                            0,
                            minMax.min - newHeight,
                            it.width.toInt() + (padding * 2),
                            (newHeight) * 2
                        )
                    }
                } else images
            }
            .doOnError { e ->
                logger.error("Error in building WaveformImage", e)
            }
            .subscribeOn(Schedulers.computation())
            .observeOnFx()
    }

    private fun drawWaveform(
        img: WritableImage,
        reader: AudioFileReader,
        width: Int,
        height: Int,
        padding: Int
    ): Pair<Int, Int> {
        val framesPerPixel = reader.totalFrames / width

        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)
        var globalMax = 1
        var globalMin = 0
        addPadding(img, 0, padding, height)
        for (i in padding until width) {
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
            globalMax = max(globalMax, min)
            globalMin = max(globalMin, max)
            val range = scaleToHeight(max, height) until scaleToHeight(min, height)
            for (j in 0 until height) {
                img.pixelWriter.setColor(i, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i, j, wavColor)
                }
            }
        }
        addPadding(img, (width + padding), (width + (padding * 2)), height)
        return Pair(scaleToHeight(globalMin, height), scaleToHeight(globalMax, height))
    }

    private fun drawImages(
        reader: AudioFileReader,
        width: Int,
        height: Int,
        padding: Int,
        minMax: MinMax
    ): List<Image> {
        var counter = 0
        var img = WritableImage(PARTIAL_IMAGE_WIDTH + 2 * padding, height)
        val images = mutableListOf(img)

        val framesPerPixel = reader.totalFrames / width

        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)
        var globalMax = 1
        var globalMin = 0
        addPadding(img, 0, padding, height)

        for (i in padding until width) {
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
            globalMax = max(globalMax, min)
            globalMin = max(globalMin, max)
            val range = scaleToHeight(max, height) until scaleToHeight(min, height)
            for (j in 0 until height) {
                img.pixelWriter.setColor(i % PARTIAL_IMAGE_WIDTH, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i % PARTIAL_IMAGE_WIDTH, j, wavColor)
                }
            }
            addPadding(img, (width + padding), (width + (padding * 2)), height)
            counter++
            if (counter == PARTIAL_IMAGE_WIDTH) {
                counter = 0
                img = WritableImage(PARTIAL_IMAGE_WIDTH + 2 * padding, height)
                images.add(img)
            }
        }

        // crop to fit last image width
        val lastImageWidth = width % PARTIAL_IMAGE_WIDTH
        if (images.size > 0 && lastImageWidth != 0) {
            images[images.size - 1] = WritableImage(
                img.pixelReader,
                0,
                0,
                lastImageWidth + (padding * 2),
                height
            )
        }
        minMax.min = scaleToHeight(globalMin, height)
        minMax.max = scaleToHeight(globalMax, height)
        return images
    }

    private fun addPadding(img: WritableImage, startX: Int, endX: Int, height: Int) {
        for (i in startX until endX) {
            for (j in 0 until height) {
                img.pixelWriter.setColor(i, j, paddingColor)
            }
        }
    }

    private fun scaleToHeight(value: Int, height: Int): Int {
        return ((value) / (SIGNED_SHORT_MAX * 2).toDouble() * height).toInt()
    }
}
