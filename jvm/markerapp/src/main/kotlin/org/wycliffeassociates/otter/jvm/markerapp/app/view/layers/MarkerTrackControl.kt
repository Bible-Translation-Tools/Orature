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
import java.awt.SystemColor.scrollbar


class MarkerTrackControl(val viewModel: VerseMarkerViewModel) : Control() {
    private val markerViewList = FXCollections.observableArrayList<ChunkMarker>()

    override fun createDefaultSkin(): Skin<*> {
        return MarkerTrackControlSkin(this)
    }
}

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region
    val marker: ChunkMarker
    val markers = FXCollections.observableArrayList<ChunkMarker>()
    private var preDragThumbPos = 0.0
    var dragStart: Point2D? = null

    init {
        marker = ChunkMarker().apply {
            markerNumberProperty.set((1).toString())
            canBeMovedProperty.set(true)
            setOnMouseClicked { me ->
//                dragStart = localToParent(me.getX(), me.getY());
//                me.consume()

                val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                println("skinnableWidth on click: ${trackWidth}")

                if (trackWidth > 0) {
                    dragStart = localToParent(me.x, me.y)
                    val clampedValue: Double = Utils.clamp(
                        0.0,
                        markerPositionProperty.value,
                        trackWidth
                    )
                    preDragThumbPos = (clampedValue - 0.0) / (trackWidth - 0.0)
                    me.consume()
                }
            }

            setOnMouseDragged { me ->
//                val cur: Point2D = localToParent(me.x, me.y)
//                if (dragStart == null) {
//                    // we're getting dragged without getting a mouse press
//                    dragStart = localToParent(me.x, me.y)
//                }
//                val dragPos = cur.x - dragStart!!.x
//
//                println("cur x is ${cur.x} dragstart is ${dragStart!!.x}")
//                this@MarkerTrackControlSkin.value = Utils.clamp(0.0, dragPos + markerPositionProperty.value, skinnable.width)
//                updateValue(this@MarkerTrackControlSkin.value)
//                me.consume()

                val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                println("skinnableWidth on drag: ${trackWidth}")


                if (trackWidth > 0.0) {
                    /*
                    ** if the tracklength isn't greater then do nothing....
                    */
                    if (trackWidth > this.width) {
                        val cur: Point2D = localToParent(me.x, me.y)
                        if (dragStart == null) {
                            // we're getting dragged without getting a mouse press
                            dragStart = localToParent(me.x, me.y)
                        }
                        val dragPos = cur.x - dragStart!!.x
                        updateValue(preDragThumbPos + dragPos / (trackWidth - this.width))
                    } else {
                        println("tracklength isn't greater than nothing")
                    }
                    me.consume()
                }
            }

            setOnMouseReleased {
                println("released")
            }
        }

        track = Region().apply {
            styleClass.add("vm-marker-track")
        }
        track.add(marker)

        marker.markerPositionProperty.onChange {
            marker.translateX = it
        }

        children.clear()
        children.addAll(track)
    }

    fun updateValue(position: Double) {
        val newValue: Double = position * (skinnable.width - 0.0) + 0.0
        if (!java.lang.Double.isNaN(newValue)) {
            println("value is $position")
            marker.markerPositionProperty.set(Utils.clamp(0.0, newValue, skinnable.width))
        } else {
            println("is NaN")
        }
    }


}
