package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.javafx.util.Utils
import javafx.geometry.Point2D
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.view.framesToPixels
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsToFrames
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class MarkerTrackControl(val markers: List<org.wycliffeassociates.otter.jvm.markerapp.app.model.ChunkMarker>) :
    Control() {

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region
    val markers = mutableListOf<ChunkMarker>()
    private val preDragThumbPos = DoubleArray(control.markers.size)
    var dragStart: Array<Point2D?> = Array(control.markers.size) { null }

    init {
        control.markers.forEachIndexed { i, mk ->
            val marker = ChunkMarker().apply {
                val pixel = framesToPixels(
                    mk.frame,
                    this@MarkerTrackControlSkin.skinnable.prefWidth.toInt(),
                    SECONDS_ON_SCREEN
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
                                it.toDouble(),
                                trackWidth.toInt(),
                                SECONDS_ON_SCREEN
                            )
                        }
                    }
                }
            }
            markers.add(marker)
        }

        track = Region().apply {
            styleClass.add("vm-marker-track")
        }

        markers.forEach { track.add(it) }

        children.clear()
        children.addAll(track)
    }

    fun updateValue(id: Int, position: Double) {
        val newValue: Double = position * skinnable.width
        if (!java.lang.Double.isNaN(newValue)) {
            val min = getMin(id, position)
            val max = getMax(id, position)
            val clamped = Utils.clamp(min, newValue, max)
            markers.get(id).markerPositionProperty.set(clamped)
        }
    }

    fun getMin(id: Int, position: Double): Double {
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

    fun getMax(id: Int, position: Double): Double {
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
