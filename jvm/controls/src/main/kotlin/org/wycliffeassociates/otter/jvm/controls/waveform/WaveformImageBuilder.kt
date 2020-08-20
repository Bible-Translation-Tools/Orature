package org.wycliffeassociates.otter.jvm.controls.waveform

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.sun.glass.ui.Screen
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue
import kotlin.math.max

const val SIGNED_SHORT_MAX = 32767

class WaveformImageBuilder {
    private val height = 65537

    fun build(
        audio: WavFile,
        padding: Int,
        wavColor: Color = Color.BLACK,
        background: Color = Color.TRANSPARENT
    ): Single<Image> {
        return Single.fromCallable {
            val width = Screen.getMainScreen().platformWidth
            if (width > 0) {
                val img = WritableImage(width + (2 * padding), height)
                val reader = WavFileReader(audio)
                val (globalMin, globalMax) = drawWaveform(img, reader, width, background, wavColor)
                val newHeight = globalMax - globalMin
                val image2 = WritableImage(
                    img.pixelReader,
                    0,
                    globalMin - newHeight - 1000,
                    width, (newHeight + 1000) * 2
                )
                image2 as Image
            } else {
                WritableImage(1, 1) as Image
            }
        }
            .subscribeOn(Schedulers.computation())
            .observeOnFx()
    }

    fun drawWaveform(
        img: WritableImage,
        reader: WavFileReader,
        width: Int,
        background: Color,
        wavColor: Color
    ): Pair<Int, Int> {
        val framesPerPixel = reader.totalFrames / width

        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)
        var globalMax = 1
        var globalMin = 0
        for (i in 0 until width) {
            reader.getPcmBuffer(bytes)
            val bb = ByteBuffer.wrap(bytes)
            bb.rewind()
            bb.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortsArray)

            // translate by half the total range of values (half a short)
            // to push everything below 0; because the image top left is 0,0
            // the absolute value will put the max values close to 0 and the
            // min values further away to the maximum
            val min = ((shortsArray.min()?.toInt() ?: 0) - SIGNED_SHORT_MAX).absoluteValue
            val max = ((shortsArray.max()?.toInt() ?: 0) - SIGNED_SHORT_MAX).absoluteValue
            globalMax = max(globalMax, min)
            globalMin = max(globalMin, max)
            val range = max until min
            for (j in 0 until height) {
                img.pixelWriter.setColor(i, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i, j, wavColor)
                }
            }
        }
        return Pair(globalMin, globalMax)
    }
}
