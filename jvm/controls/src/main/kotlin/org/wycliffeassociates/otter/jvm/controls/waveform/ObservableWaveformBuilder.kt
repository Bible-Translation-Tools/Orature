package org.wycliffeassociates.otter.jvm.controls.waveform

import com.sun.glass.ui.Screen
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import javafx.scene.image.Image
import javafx.scene.image.WritableImage
import javafx.scene.paint.Color
import kotlin.math.absoluteValue
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader

const val SIGNED_SHORT_MAX = 32767

class ObservableWaveformBuilder {
    private val logger = LoggerFactory.getLogger(ObservableWaveformBuilder::class.java)

    private var reader: AudioFileReader? = null

    private var wavColor: Color = Color.BLACK
    private var background: Color = Color.TRANSPARENT

    private val partialImageWidth = Screen.getMainScreen().platformWidth
    private var width: Int = Screen.getMainScreen().platformWidth
    private var height: Int = Screen.getMainScreen().platformHeight

    private val started = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    private val images = mutableListOf<Image>()
    private val subscribers = mutableListOf<ObservableEmitter<Image>>()

    private fun start() {
        synchronized(started) {
            // return if already started (the swap happens if starting the first time, which then returns true)
            if (!started.compareAndSet(false, true)) {
                return
            }
        }
        cancelled.set(false)
        reader?.let { reader ->
            try {
                reader.open()
                drawPartialImages(reader, width, height, subscribers)
            } finally {
                reader.release()
            }
        }
    }

    fun cancel() {
        synchronized(started) {
            cancelled.set(true)
            started.set(false)
            subscribers.forEach {
                if (!it.isDisposed) it.onComplete()
            }
            images.clear()
            subscribers.clear()
        }
    }

    fun build(
        reader: AudioFileReader,
        width: Int = Screen.getMainScreen().platformWidth,
        height: Int = Screen.getMainScreen().platformHeight,
        wavColor: Color = Color.BLACK,
        background: Color = Color.TRANSPARENT
    ): Single<Image> {
        this.wavColor = wavColor
        this.background = background
        return Single
            .fromCallable {
                if (width > 0) {
                    val framesPerPixel = reader.totalFrames / width
                    val img = WritableImage(width, height)
                    reader.open()
                    renderImage(img, reader, width, height, framesPerPixel)
                    img
                } else {
                    WritableImage(1, 1) as Image
                }
            }
            .doOnError { e ->
                logger.error("Error in building WaveformImage", e)
            }
            .doAfterTerminate {
                reader.release()
            }
            .subscribeOn(Schedulers.computation())
    }

    fun buildAsync(
        reader: AudioFileReader,
        width: Int,
        height: Int,
        wavColor: Color = Color.BLACK,
        background: Color = Color.TRANSPARENT
    ): Observable<Image> {
        this.reader = reader
        this.width = width
        this.height = height
        this.wavColor = wavColor
        this.background = background

        return Observable.create<Image?> { emitter ->
            emitter.setDisposable(
                object : Disposable {
                    val disposed = AtomicBoolean(false)

                    override fun dispose() {
                        disposed.set(true)
                    }

                    override fun isDisposed(): Boolean {
                        return disposed.get()
                    }
                }
            )
            synchronized(this@ObservableWaveformBuilder) {
                for (image in images) {
                    emitter.onNext(image)
                }
                subscribers.add(emitter)
            }
            start()
        }.subscribeOn(Schedulers.io())
    }

    private fun drawPartialImages(
        reader: AudioFileReader,
        width: Int,
        height: Int,
        subscribers: List<ObservableEmitter<Image>>
    ) {
        val framesPerPixel = reader.totalFrames / width
        var img = WritableImage(partialImageWidth, height)
        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)
        var counter = 0

        // render fixed-width images until the last one
        val lastImageWidth = width % partialImageWidth
        for (i in 0 until width - lastImageWidth) {
            if (cancelled.get()) break
            val range = computeWaveRange(reader, height, shortsArray, bytes)

            for (j in 0 until height) {
                img.pixelWriter.setColor(i % partialImageWidth, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i % partialImageWidth, j, wavColor)
                }
            }

            counter++
            if (counter == partialImageWidth) {
                synchronized(this@ObservableWaveformBuilder) {
                    images.add(img)
                    subscribers.forEach {
                        if (!it.isDisposed) {
                            it.onNext(img)
                        }
                    }
                }
                img = WritableImage(partialImageWidth, height)
                counter = 0
            }
        }

        // render final image with exact width
        if (lastImageWidth != 0 && !cancelled.get()) {
            img = WritableImage(
                img.pixelReader,
                0,
                0,
                lastImageWidth,
                height
            )
            renderImage(img, reader, lastImageWidth, height, framesPerPixel)
            if (!cancelled.get()) {
                synchronized(this@ObservableWaveformBuilder) {
                    images.add(img)
                    subscribers.forEach {
                        if (!it.isDisposed) {
                            it.onNext(img)
                        }
                    }
                }
            }
        }

        synchronized(this@ObservableWaveformBuilder) {
            subscribers.forEach {
                if (!it.isDisposed) it.onComplete()
            }
        }
    }

    private fun scaleToHeight(value: Int, height: Int): Int {
        return ((value) / (SIGNED_SHORT_MAX * 2).toDouble() * height).toInt()
    }

    private fun renderImage(
        img: WritableImage,
        reader: AudioFileReader,
        width: Int,
        height: Int,
        framesPerPixel: Int
    ) {
        val shortsArray = ShortArray(framesPerPixel)
        val bytes = ByteArray(framesPerPixel * 2)

        for (i in 0 until width) {
            val range = computeWaveRange(
                reader,
                height,
                shortsArray,
                bytes
            )

            for (j in 0 until height) {
                img.pixelWriter.setColor(i, j, background)
                if (j in range) {
                    img.pixelWriter.setColor(i, j, wavColor)
                }
            }
        }
    }

    private fun computeWaveRange(
        reader: AudioFileReader,
        height: Int,
        shortsArray: ShortArray,
        bytes: ByteArray
    ): IntRange {
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
