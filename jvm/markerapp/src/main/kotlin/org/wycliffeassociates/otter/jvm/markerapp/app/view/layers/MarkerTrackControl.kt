package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.javafx.util.Utils
import javafx.collections.FXCollections
import javafx.geometry.Point2D
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.view.framesToPixels
import org.wycliffeassociates.otter.jvm.markerapp.app.view.pixelsToFrames
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.SECONDS_ON_SCREEN
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.add
import tornadofx.onChange

class MarkerTrackControl(val markers: List<org.wycliffeassociates.otter.jvm.markerapp.app.model.ChunkMarker>) : Control() {
    private val markerViewList = FXCollections.observableArrayList<ChunkMarker>()

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region
    val markers = mutableListOf<ChunkMarker>()
    private val preDragThumbPos = DoubleArray(control.markers.size)
    var dragStart: Array<Point2D?> = Array<Point2D?>(control.markers.size) { null }

    init {
        control.markers.forEachIndexed { i, mk ->
            val marker = ChunkMarker().apply {
                val pixel = framesToPixels(
                    mk.frame,
                    this@MarkerTrackControlSkin.skinnable.prefWidth.toInt(),
                    SECONDS_ON_SCREEN
                ).toDouble()
                println("frame: ${mk.frame}, pixel: ${pixel}")
                markerNumberProperty.set(mk.label)
                canBeMovedProperty.set(mk.placed)
                markerPositionProperty.set(
                    pixel
                )
                setOnMouseClicked { me ->
                    val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                    println("skinnableWidth on click: ${trackWidth}")

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
                    val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                    if (trackWidth > 0.0) {
                        if (trackWidth > this.width) {
                            val cur: Point2D = localToParent(me.x, me.y)
                            if (dragStart[i] == null) {
                                // we're getting dragged without getting a mouse press
                                dragStart[i] = localToParent(me.x, me.y)
                            }
                            val dragPos = cur.x - dragStart[i]!!.x
                            updateValue(i, preDragThumbPos[i] + dragPos / (trackWidth - this.width))
                        }
                        me.consume()
                    }
                }

                markerPositionProperty.onChangeAndDoNow {
                    it?.let {
                        translateX = it.toDouble()
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
            println("value is $position")
            val min = getMin(id, position)
            val max = getMax(id, position)
            markers.get(id).markerPositionProperty.set(Utils.clamp(min, newValue, max))
        }
    }

    fun getMin(id: Int, position: Double): Double {
        val previousMaker = if (id > 0) {
            markers.get(id - 1)
        } else {
            null
        }
        return previousMaker?.let {
            it.markerPositionProperty.value.toInt() + it.width
        } ?: 0.0
    }

    fun getMax(id: Int, position: Double): Double {
        val previousMaker = if (id < markers.size - 1) {
            markers.get(id + 1)
        } else {
            null
        }
        return previousMaker?.let {
            it.markerPositionProperty.value - it.width
        } ?: skinnable.width
    }
}
