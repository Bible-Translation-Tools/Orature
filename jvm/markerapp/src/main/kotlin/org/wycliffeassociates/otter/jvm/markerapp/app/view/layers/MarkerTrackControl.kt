package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.javafx.util.Utils
import javafx.beans.binding.Bindings
import javafx.beans.binding.DoubleBinding
import javafx.beans.binding.DoubleExpression
import javafx.geometry.Point2D
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.markerapp.app.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.markerapp.app.view.framesToPixels
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsToFrames
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.util.concurrent.Callable

class MarkerTrackControl(val markers: List<ChunkMarkerModel>, val highlightState: List<MarkerHighlightState>) :
    Control() {

    fun refreshMarkers() {
        (skin as? MarkerTrackControlSkin)?.let { it.refreshMarkers() }
    }

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region
    val markers = mutableListOf<ChunkMarker>()

    val highlights = mutableListOf<Rectangle>()

    private val preDragThumbPos = DoubleArray(control.markers.size)
    var dragStart: Array<Point2D?> = Array(control.markers.size) { null }

    fun refreshMarkers() {
        if (skinnable.width > 0) {
            skinnable.markers.forEachIndexed { index, chunkMarker ->
                val marker = markers[index]
                marker.isPlacedProperty.set(chunkMarker.placed)
                marker.markerPositionProperty.set(
                    framesToPixels(
                        chunkMarker.frame
                    ).toDouble()
                )
                marker.markerNumberProperty.set(chunkMarker.label)
            }
        }
    }

    init {
        control.markers.forEachIndexed { i, mk ->
            val marker = ChunkMarker().apply {
                val pixel = framesToPixels(
                    mk.frame
                ).toDouble()

                isPlacedProperty.set(mk.placed)
                markerNumberProperty.set(mk.label)
                canBeMovedProperty.set(i != 0)
                markerPositionProperty.set(pixel)

                setOnMouseClicked { me ->
                    val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                    if (trackWidth > 0) {
                        dragStart[i] = localToParent(me.x, me.y)
                        val clampedValue: Double = Utils.clamp(
                            0.0,
                            markerPositionProperty.value,
                            trackWidth
                        )
                        preDragThumbPos[i] = clampedValue / trackWidth
                        me.consume()
                    }
                }

                setOnMouseDragged { me ->
                    if (!canBeMovedProperty.value) return@setOnMouseDragged
                    val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                    if (trackWidth > 0.0) {
                        if (trackWidth > this.width) {
                            val cur: Point2D = localToParent(me.x, me.y)
                            if (dragStart[i] == null) {
                                // we're getting dragged without getting a mouse press
                                dragStart[i] = localToParent(me.x, me.y)
                                val clampedValue: Double = Utils.clamp(
                                    0.0,
                                    markerPositionProperty.value,
                                    trackWidth
                                )
                                preDragThumbPos[i] = clampedValue / trackWidth
                            }
                            val dragPos = cur.x - dragStart[i]!!.x
                            updateValue(i, preDragThumbPos[i] + dragPos / (trackWidth - this.width))
                        }
                        me.consume()
                    }
                }

                markerPositionProperty.onChangeAndDoNow {
                    it?.let {
                        val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                        translateX = it.toDouble()
                        if (trackWidth > 0) {
                            control.markers.get(i).frame = pixelsToFrames(
                                it.toDouble()
                            )
                        }
                    }
                }
            }

            val rect = Rectangle(400.0, 0.0).apply {
                style {
                    if (i % 2 == 0) {
                        fill = Paint.valueOf("#015ad933")
                    } else {
                        fill = Paint.valueOf("#1edd7633")
                    }
                }
            }
            rect.heightProperty().bind(skinnable.heightProperty())
            rect.translateXProperty().bind(marker.translateXProperty())
            rect.visibleProperty().bind(marker.visibleProperty())

            markers.add(marker)
            highlights.add(rect)
        }

        track = Region().apply {
            styleClass.add("vm-marker-track")
        }

        highlights.forEach { track.add(it) }
        markers.forEach { track.add(it) }

        highlights.forEachIndexed { i, rect ->
            val endPos = skinnable.widthProperty()

            if (i + 1 < highlights.size) {
                highlights[i + 1]?.let { next ->
                    val nextVis = next.visibleProperty()
                    val nextPos = next.translateXProperty()
                    val highlightWidth = Bindings.createDoubleBinding(
                        Callable {
                            return@Callable if (nextVis.value) {
                                nextPos.value - rect.translateXProperty().value
                            } else {
                                endPos.value - rect.translateXProperty().value
                            }
                        },
                        nextVis,
                        nextPos,
                        endPos,
                        rect.translateXProperty(),
                        next.translateXProperty()
                    )
                    rect.widthProperty().bind(highlightWidth)
                }
            } else {
                rect.widthProperty().bind(endPos.minus(rect.translateXProperty()))
            }
            skinnable.highlightState[i].visibility.bind(rect.visibleProperty())
            skinnable.highlightState[i].translate.bind(rect.translateXProperty())
            skinnable.highlightState[i].width.bind(rect.widthProperty())
        }

        children.clear()
        children.addAll(track)
    }

    fun updateValue(id: Int, position: Double) {
        val newValue: Double = position * skinnable.width
        if (!newValue.isNaN()) {
            val min = getMin(id)
            val max = getMax(id)
            val clamped = Utils.clamp(min, newValue, max)
            markers.get(id).markerPositionProperty.set(clamped)
        }
    }

    fun getMin(id: Int): Double {
        val placedMarkers = markers.filter { it.isPlacedProperty.value }
        val previousMaker = if (id > 0) {
            placedMarkers.get(id - 1)
        } else {
            null
        }
        return previousMaker?.let {
            it.markerPositionProperty.value.toInt() + it.width
        } ?: 0.0
    }

    fun getMax(id: Int): Double {
        val placedMarkers = markers.filter { it.isPlacedProperty.value }
        val previousMaker = if (id < placedMarkers.size - 1) {
            placedMarkers.get(id + 1)
        } else {
            null
        }
        return previousMaker?.let {
            it.markerPositionProperty.value - it.width
        } ?: skinnable.width
    }
}
