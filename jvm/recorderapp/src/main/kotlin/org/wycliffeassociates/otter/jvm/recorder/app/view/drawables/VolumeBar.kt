package org.wycliffeassociates.otter.jvm.recorder.app.view.drawables

import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.absoluteValue
import kotlin.math.max

const val RANGE = 32767.0

class VolumeBar(stream: Observable<ByteArray>) : Drawable {

    private val logger = LoggerFactory.getLogger(VolumeBar::class.java)

    companion object {
        var decibleAtom = 0
    }

    private val xPoints = DoubleArray(4)
    private val yPoints = DoubleArray(4)

    private var dbNone: Double = 0.0
    private var dbLow: Double = 0.0
    private var dbGood: Double = 0.0
    private var dbHigh: Double = 0.0
    private var dbMax: Double = 0.0

    private val lowColor: Paint = Paint.valueOf("#085394")
    private val goodColor: Paint = Paint.valueOf("#45818E")
    private val highColor: Paint = Paint.valueOf("#93C47D")
    private val maxColor: Paint = Paint.valueOf("#FFE599")
    private val defaultColor: Paint = Paint.valueOf("#CF2A27")

    init {
        val bb = ByteBuffer.allocate(20480)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        stream.subscribeOn(Schedulers.io())
            .subscribe(
                {
                    bb.put(it)
                    bb.position(0)
                    var max = 0
                    while (bb.hasRemaining()) {
                        val db = bb.short.toInt().absoluteValue
                        max = max(db, max)
                    }
                    decibleAtom = max
                    bb.clear()
                }, { e ->
                    logger.error("Error in the volume bar", e)
                }
            )
    }

    override fun draw(context: GraphicsContext, canvas: Canvas) {
        calculateDbPixelLocations(canvas.height)
        drawBar(canvas, context)
    }

    private fun calculateDbPixelLocations(height: Double) {
        dbNone = getDbLevel(0, height)
        dbLow = getDbLevel(2067, height) // -24 decibel
        dbGood = getDbLevel(4125, height) // -18 decibel
        dbHigh = getDbLevel(23197, height) // -3 decibel
        dbMax = getDbLevel(32767, height)
    }

    private fun drawBar(canvas: Canvas, context: GraphicsContext) {
        val decible = decibleAtom
        val currentDb = getDbLevel(decible, canvas.height)
        val currentDbNeg = getDbLevel(decible * -1, canvas.height)
        setColor(currentDb, context)

        xPoints[0] = 0.0
        yPoints[0] = currentDb

        xPoints[1] = canvas.width
        yPoints[1] = currentDb

        xPoints[2] = canvas.width
        yPoints[2] = currentDbNeg

        xPoints[3] = 0.0
        yPoints[3] = currentDbNeg

        context.fillPolygon(
            xPoints,
            yPoints,
            4
        )
    }

    private fun getDbLevel(db: Int, height: Double): Double {
        return (db / (RANGE) * (height / 2.0)) + height / 2.0
    }

    private fun setColor(currentDb: Double, ctx: GraphicsContext) {
        ctx.fill = when {
            currentDb < dbLow -> lowColor
            currentDb < dbGood -> goodColor
            currentDb < dbHigh -> highColor
            currentDb < dbMax -> maxColor
            else -> defaultColor
        }
    }
}
