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
                val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                println("skinnableWidth on click: ${trackWidth}")

                if (trackWidth > 0) {
                    dragStart = localToParent(me.x, me.y)
                    val clampedValue: Double = Utils.clamp(
                        0.0,
                        markerPositionProperty.value,
                        trackWidth
                    )
                    preDragThumbPos = clampedValue / trackWidth
                    me.consume()
                }
            }

            setOnMouseDragged { me ->
                val trackWidth = this@MarkerTrackControlSkin.skinnable.width
                if (trackWidth > 0.0) {
                    if (trackWidth > this.width) {
                        val cur: Point2D = localToParent(me.x, me.y)
                        if (dragStart == null) {
                            // we're getting dragged without getting a mouse press
                            dragStart = localToParent(me.x, me.y)
                        }
                        val dragPos = cur.x - dragStart!!.x
                        updateValue(preDragThumbPos + dragPos / (trackWidth - this.width))
                    }
                    me.consume()
                }
            }

            markerPositionProperty.onChange {
                translateX = it
            }
        }
        markers.add(marker)

        track = Region().apply {
            styleClass.add("vm-marker-track")
        }

        markers.forEach { track.add(marker) }

        children.clear()
        children.addAll(track)
    }

    fun updateValue(position: Double) {
        val newValue: Double = position * skinnable.width
        if (!java.lang.Double.isNaN(newValue)) {
            println("value is $position")
            markers.get(0).markerPositionProperty.set(Utils.clamp(0.0, newValue, skinnable.width))
        }
    }
}
