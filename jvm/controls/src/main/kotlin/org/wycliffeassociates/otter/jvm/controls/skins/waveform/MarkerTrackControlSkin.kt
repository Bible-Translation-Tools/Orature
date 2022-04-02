/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.skins.waveform

import com.sun.javafx.util.Utils
import javafx.beans.binding.Bindings
import javafx.geometry.Point2D
import javafx.scene.control.SkinBase
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.ChunkMarker
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.util.concurrent.Callable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChunkMarkerModel
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.controls.waveform.MarkerTrackControl

private const val MOVE_MARKER_INTERVAL = 0.001

class MarkerTrackControlSkin(control: MarkerTrackControl) : SkinBase<MarkerTrackControl>(control) {

    val track: Region = Region().apply {
        styleClass.add("scrolling-waveform__marker-track")
    }

    val markers = mutableListOf<ChunkMarker>()

    private val highlights = mutableListOf<Rectangle>()

    private var preDragThumbPos = DoubleArray(control.markers.size)
    var dragStart: Array<Point2D?> = Array(control.markers.size) { null }

    private val focusedMarkerProperty = SimpleObjectProperty<ChunkMarker>()

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

    fun resetState() {
        markers.clear()
        highlights.clear()
        preDragThumbPos = DoubleArray(skinnable.markers.size)
        dragStart = Array(skinnable.markers.size) { null }
    }

    private fun createMarker(i: Int, mk: ChunkMarkerModel): ChunkMarker {
        return ChunkMarker().apply {
            val pixel = framesToPixels(
                mk.frame
            ).toDouble()

            isPlacedProperty.set(mk.placed)
            markerIdProperty.set(i)
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
                this.requestFocus()
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
                        skinnable.markers[i].frame = pixelsToFrames(
                            it.toDouble()
                        )
                    }
                }
            }

            focusedProperty().onChange {
                if (it) focusedMarkerProperty.set(this)
            }
        }
    }

    private fun createHighlight(i: Int, mk: ChunkMarkerModel): Rectangle {
        return Rectangle().apply {
            skinnable.highlightState[i].styleClass.onChangeAndDoNow {
                styleClass.setAll(it)
            }
        }
    }

    private fun initializeMarkers() {
        skinnable.markers.forEachIndexed { i, mk ->
            val marker = createMarker(i, mk)
            val rect = createHighlight(i, mk)

            rect.heightProperty().bind(skinnable.heightProperty())
            rect.translateXProperty().bind(marker.translateXProperty())
            rect.visibleProperty().bind(marker.visibleProperty())

            markers.add(marker)
            highlights.add(rect)
        }
    }

    init {
        skinnable.markers.onChangeAndDoNow {
            resetState()
            initializeMarkers()

            track.getChildList()?.clear()
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
        }

        control.setOnKeyPressed { e ->
            when (e.code) {
                KeyCode.LEFT, KeyCode.RIGHT -> {
                    moveMarker(e.code)
                    e.consume()
                }
                KeyCode.TAB -> {
                    if (e.isShiftDown) {
                        control.onSeekPreviousProperty.value?.invoke()
                    } else {
                        control.onSeekNextProperty.value?.invoke()
                    }
                    focusMarker(e)
                }
            }
        }

        control.setOnKeyReleased {
            when (it.code) {
                KeyCode.ENTER, KeyCode.SPACE -> it.consume()
            }
        }

        children.clear()
        children.addAll(track)
    }

    private fun moveMarker(code: KeyCode) {
        focusedMarkerProperty.value?.let { marker ->
            val id = marker.markerIdProperty.value
            if (id == 0) return // don't move the first marker

            val position = marker.markerPositionProperty.value
            val percent = position / skinnable.width
            val moveTo = if (code == KeyCode.LEFT) {
                percent - MOVE_MARKER_INTERVAL
            } else {
                percent + MOVE_MARKER_INTERVAL
            }
            updateValue(marker.markerIdProperty.value, moveTo)
        }
    }

    private fun focusMarker(event: KeyEvent) {
        val location = skinnable.onLocationRequestProperty.value?.invoke() ?: 0
        val position = framesToPixels(location).toDouble()

        markers.singleOrNull { it.markerPositionProperty.value == position }?.let {
            it.requestFocus()
            event.consume()
        }
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
