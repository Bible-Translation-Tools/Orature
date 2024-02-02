/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.marker

import com.sun.javafx.scene.NodeHelper
import com.sun.javafx.scene.traversal.Direction
import com.sun.javafx.scene.traversal.TraversalMethod
import com.sun.javafx.util.Utils
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Point2D
import javafx.scene.layout.Region
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*
import java.util.concurrent.Callable
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.jvm.controls.event.MarkerMovedEvent
import org.wycliffeassociates.otter.common.domain.model.MarkerItem
import org.wycliffeassociates.otter.jvm.controls.model.framesToPixels
import org.wycliffeassociates.otter.jvm.controls.model.pixelsToFrames
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNowWithDisposer

const val MOVE_MARKER_INTERVAL = 0.001
const val MARKER_COUNT = 500

open class MarkerTrackControl : Region() {

    val markers = observableListOf<MarkerItem>()
    val canMoveMarkerProperty = SimpleBooleanProperty(true)
    val canDeleteMarkerProperty = SimpleBooleanProperty(true)
    val onPositionChangedProperty = SimpleObjectProperty<(Int, Double) -> Unit>()
    val onSeekPreviousProperty = SimpleObjectProperty<() -> Unit>()
    val onSeekNextProperty = SimpleObjectProperty<() -> Unit>()
    val onSeekProperty = SimpleObjectProperty<(Int) -> Unit>()
    val onLocationRequestProperty = SimpleObjectProperty<() -> Int>()

    fun setOnPositionChanged(op: (Int, Double) -> Unit) {
        onPositionChangedProperty.set(op)
    }

    fun setOnLocationRequest(op: () -> Int) {
        onLocationRequestProperty.set(op)
    }

    protected val _markers = mutableListOf<MarkerControl>()
    protected val highlights = mutableListOf<Rectangle>()

    private var preDragThumbPos = DoubleArray(markers.size)
    var dragStart: Array<Point2D?> = Array(markers.size) { null }

    private val focusedMarkerProperty = SimpleObjectProperty<MarkerControl>()
    private val listeners = mutableListOf<ListenerDisposer>()

    fun initialize() {
        resetState()
        preallocateMarkers()

        highlights.forEach { add(it) }
        _markers.forEach { add(it) }
        refreshMarkers()
        refreshHighlights()

        markers.onChangeAndDoNowWithDisposer {
            it.sortedBy { it.frame }
            refreshMarkers()
            refreshHighlights()
        }.also(listeners::add)
    }

    fun refreshMarkers() {
        markers.forEachIndexed { index, chunkMarker ->
            val marker = _markers[index]
            marker.markerIdProperty.set(chunkMarker.id)
            marker.isPlacedProperty.set(chunkMarker.placed)
            marker.markerPositionProperty.set(
                framesToPixels(
                    chunkMarker.frame
                ).toDouble()
            )
            marker.markerNumberProperty.set(markerDisplayLabel(chunkMarker))
            highlights[index].apply {
                styleClass.clear()
                when (index % 2 == 1) {
                    true -> {
                        styleClass.setAll("scrolling-waveform__highlight--primary")
                        styleClass.removeAll("scrolling-waveform__highlight--secondary")
                    }
                    false -> {
                        styleClass.setAll("scrolling-waveform__highlight--secondary")
                        styleClass.removeAll("scrolling-waveform__highlight--primary")
                    }
                }
            }
            highlights[index].translateXProperty().bind(marker.translateXProperty())
            highlights[index].visibleProperty().bind(marker.visibleProperty())
        }
        for (i in markers.size until _markers.size) {
            _markers[i].isPlacedProperty.set(false)
        }
    }

    fun resetState() {
        _markers.clear()
        highlights.clear()
        preDragThumbPos = DoubleArray(MARKER_COUNT)
        dragStart = Array(MARKER_COUNT) { null }
        getChildList()?.clear()
        listeners.forEach(ListenerDisposer::dispose)
        listeners.clear()
    }

    open fun createMarker(): MarkerControl = ChunkMarker()
    private fun markerDisplayLabel(chunkMarker: MarkerItem): String {
        return when (chunkMarker.marker) {
            is ChapterMarker -> "c${chunkMarker.label}"
            else -> chunkMarker.label
        }
    }

    protected fun createMarker(i: Int, mk: MarkerItem): MarkerControl {
        val container = this
        var startPos = 0.0
        return createMarker().apply {
            val pixel = framesToPixels(
                mk.frame
            ).toDouble()

            isPlacedProperty.set(mk.placed)
            markerIndexProperty.set(i)
            markerNumberProperty.set(markerDisplayLabel(mk))
            canBeMovedProperty.bind(canMoveMarkerProperty)
            canBeDeletedProperty.bind(canDeleteMarkerProperty)
            markerPositionProperty.set(pixel)

            setOnDragStart { me ->
                val trackWidth = container.width
                if (trackWidth > 0) {
                    dragStart[i] = localToParent(me.x, me.y)
                    val clampedValue: Double = Utils.clamp(
                        0.0,
                        markerPositionProperty.value,
                        trackWidth
                    )
                    startPos = clampedValue
                    preDragThumbPos[i] = clampedValue / trackWidth
                    me.consume()
                }
                togglePseudoClass("dragging", true)
                this.requestFocus()
            }

            setOnDrag { me ->
                if (!canBeMovedProperty.value) return@setOnDrag
                val trackWidth = container.width
                if (trackWidth > 0.0) {
                    if (trackWidth > this.width) {
                        val pos: Point2D = localToParent(me.x, me.y)
                        if (dragStart[i] == null) {
                            // we're getting dragged without getting a mouse press
                            dragStart[i] = pos
                            val clampedValue: Double = Utils.clamp(
                                0.0,
                                markerPositionProperty.value,
                                trackWidth
                            )
                            preDragThumbPos[i] = clampedValue / trackWidth
                        }
                        val dragPos = pos.x - dragStart[i]!!.x
                        updateValue(i, preDragThumbPos[i] + dragPos / (trackWidth - this.width))
                        onPositionChangedProperty.value.invoke(i, _markers[i].markerPositionProperty.value / trackWidth)
                    }
                    me.consume()
                }
            }

            setOnDragFinish {
                if (container.width > 0) {
                    val start = pixelsToFrames(startPos)
                    val end = pixelsToFrames(markerPositionProperty.value)
                    if (start != end) {
                        FX.eventbus.fire(MarkerMovedEvent(markerIdProperty.value, start, end))
                    }
                }
                togglePseudoClass("dragging", false)
            }

            markerPositionProperty.onChangeAndDoNow {
                it?.let {
                    translateX = it.toDouble()
                }
            }

            focusVisibleProperty().onChange {
                if (it) {
                    // seeks to focused marker (focus-visible)
                    val markerFrame = pixelsToFrames(markerPositionProperty.value)
                    onSeekProperty.value?.invoke(markerFrame)
                    focusedMarkerProperty.set(this)
                }
            }
        }
    }

    protected fun createHighlight(i: Int, mk: MarkerItem): Rectangle {
        return Rectangle().apply {
            when (i % 2 == 0) {
                true -> styleClass.setAll("scrolling-waveform__highlight--primary")
                false -> styleClass.setAll("scrolling-waveform__highlight--secondary")
            }
            mouseTransparentProperty().set(true)
            pickOnBoundsProperty().set(false)
            isManaged = false
        }
    }

    protected open fun preallocateMarkers() {
        for (i in 0 until MARKER_COUNT) {
            val mk = MarkerItem(VerseMarker(i, i, 0), false)
            val marker = createMarker(i, mk)
            val rect = createHighlight(i, mk)
            rect.heightProperty().bind(heightProperty().minus(40.0))
            rect.translateXProperty().bind(marker.translateXProperty())
            rect.visibleProperty().bind(marker.visibleProperty())

            _markers.add(marker)
            highlights.add(rect)
        }
    }

    private fun refreshHighlights() {
        highlights.forEachIndexed { i, rect ->
            val endPos = widthProperty()

            if (i + 1 < highlights.size) {
                highlights[i + 1].let { next ->
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
        }
    }

    init {
        initialize()

        // Makes the region mouse transparent but not children
        pickOnBoundsProperty().set(false)

        setOnKeyPressed { e ->
            when (e.code) {
                KeyCode.LEFT, KeyCode.RIGHT -> {
                    if (canMoveMarkerProperty.value) {
                        moveMarker(e.code)
                    }
                    e.consume()
                }
                KeyCode.TAB -> {
                    if (e.isControlDown) {
                        // Ctrl + Tab should move the focus out of the markers area and into the next node
                        NodeHelper.traverse(this, Direction.NEXT_IN_LINE, TraversalMethod.KEY)
                    }
                }

                else -> {}
            }
        }

        setOnKeyReleased {
            when (it.code) {
                KeyCode.ENTER, KeyCode.SPACE -> it.consume()
                else -> {}
            }
        }
    }

    private fun moveMarker(code: KeyCode) {
        focusedMarkerProperty.value?.let { marker ->
            val position = marker.markerPositionProperty.value
            val percent = position / width
            val moveTo = if (code == KeyCode.LEFT) {
                percent - MOVE_MARKER_INTERVAL
            } else {
                percent + MOVE_MARKER_INTERVAL
            }
            updateValue(marker.markerIndexProperty.value, moveTo)
            // notify changes for model's undo/redo history update
            val start = pixelsToFrames(position)
            val end = pixelsToFrames(marker.markerPositionProperty.value)
            FX.eventbus.fire(MarkerMovedEvent(marker.markerIdProperty.value, start, end))
        }
    }

    private fun focusMarker(event: KeyEvent) {
        val location = onLocationRequestProperty.value?.invoke() ?: 0
        val position = framesToPixels(location).toDouble()

        _markers.singleOrNull { it.markerPositionProperty.value == position }?.let {
            it.requestFocus()
            event.consume()
        }
    }

    fun updateValue(id: Int, position: Double) {
        val newValue: Double = position * width
        if (!newValue.isNaN()) {
            val min = getMin(id)
            val max = getMax(id)
            val clamped = Utils.clamp(min, newValue, max)
            _markers.get(id).markerPositionProperty.set(clamped)
        }
    }

    fun getMin(id: Int): Double {
        val placedMarkers = _markers.filter { it.isPlacedProperty.value }
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
        val placedMarkers = _markers.filter { it.isPlacedProperty.value }
        val previousMaker = if (id < placedMarkers.size - 1) {
            placedMarkers.get(id + 1)
        } else {
            null
        }
        return previousMaker?.let {
            it.markerPositionProperty.value - it.width
        } ?: width
    }
}
