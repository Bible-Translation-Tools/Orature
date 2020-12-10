package org.wycliffeassociates.otter.jvm.markerapp.app.view.layers

import com.sun.javafx.util.Utils
import javafx.collections.FXCollections
import javafx.geometry.Point2D
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.control.SkinBase
import javafx.scene.layout.Region
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.markerapp.app.viewmodel.VerseMarkerViewModel
import tornadofx.add
import tornadofx.onChange

class MarkerTrackControl(val markerCount: Int, val viewModel: VerseMarkerViewModel) : Control() {
    private val markerViewList = FXCollections.observableArrayList<ChunkMarker>()

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region
    val markers = mutableListOf<ChunkMarker>()
    private val preDragThumbPos = DoubleArray(control.markerCount)
    var dragStart: Array<Point2D?> = Array<Point2D?>(control.markerCount) { null }

    init {
        for (i in 0 until control.markerCount) {
            val marker = ChunkMarker().apply {
                markerNumberProperty.set((i).toString())
                canBeMovedProperty.set(true)
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

                markerPositionProperty.onChange {
                    translateX = it
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
            markers.get(id).markerPositionProperty.set(Utils.clamp(0.0, newValue, skinnable.width))
        }
    }
}
