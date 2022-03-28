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
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.controllers.SeekSpeed
import org.wycliffeassociates.otter.jvm.controls.utils.fitToHeight
import org.wycliffeassociates.otter.jvm.markerapp.app.model.MarkerHighlightState
import org.wycliffeassociates.otter.jvm.markerapp.app.view.layers.MarkerTrackControl
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class WaveformFrame(
    topTrack: Node? = null,
    bottomTrack: Node? = null
) : BorderPane() {

    private val onWaveformClickedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onWaveformDragReleasedProperty = SimpleObjectProperty<(pixel: Double) -> Unit>()
    private val onRewindProperty = SimpleObjectProperty<(SeekSpeed, (Boolean) -> Unit) -> Unit>()
    private val onFastForwardProperty = SimpleObjectProperty<(SeekSpeed, (Boolean) -> Unit) -> Unit>()
    private val onToggleMediaProperty = SimpleObjectProperty<() -> Unit>()
    private val onSeekPreviousProperty = SimpleObjectProperty<() -> Unit>()
    private val onSeekNextProperty = SimpleObjectProperty<() -> Unit>()

    val framePositionProperty = SimpleDoubleProperty(0.0)

    fun onWaveformClicked(op: () -> Unit) {
        onWaveformClickedProperty.set(EventHandler { op.invoke() })
    }

    fun onWaveformDragReleased(op: (pixel: Double) -> Unit) {
        onWaveformDragReleasedProperty.set(op)
    }

    fun onRewind(op: (SeekSpeed, (Boolean) -> Unit) -> Unit) {
        onRewindProperty.set(op)
    }

    fun onFastForward(op: (SeekSpeed, (Boolean) -> Unit) -> Unit) {
        onFastForwardProperty.set(op)
    }

    fun onToggleMedia(op: () -> Unit) {
        onToggleMediaProperty.set(op)
    }

    fun onSeekPrevious(op: () -> Unit) {
        onSeekPreviousProperty.set(op)
    }

    fun onSeekNext(op: () -> Unit) {
        onSeekNextProperty.set(op)
    }

    var dragStart: Point2D? = null
    private var dragContextX = 0.0
    var imageHolder: HBox? = null
    lateinit var imageRegion: Region
    lateinit var highlightHolder: StackPane
    var resumeAfterScroll = false

    init {
        addClass("vm-waveform-frame")

        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            bindTranslateX()

            hgrow = Priority.ALWAYS
            vgrow = Priority.ALWAYS

            top {
                region {
                    styleClass.add("vm-waveform-frame__top-track")
                    topTrack?.let {
                        add(it.apply {
                            val me = (it as MarkerTrackControl)
                            me.onSeekPreviousProperty.bind(this@WaveformFrame.onSeekPreviousProperty)
                            me.onSeekNextProperty.bind(this@WaveformFrame.onSeekNextProperty)
                        })
                    }
                }
            }

            center {
                region {
                    imageRegion = this
                    stackpane {
                        highlightHolder = this
                        styleClass.add("vm-waveform-frame__center")
                        alignment = Pos.CENTER

                        fitToParentHeight()
                        hbox {
                            imageHolder = this@hbox
                        }
                    }
                }
            }

            bottom {
                region {
                    styleClass.add("vm-waveform-frame__bottom-track")
                    bottomTrack?.let {
                        add(it)
                    }
                }
            }

            setOnMousePressed { me ->
                onWaveformClickedProperty.get().handle(ActionEvent())
                val trackWidth = this.width
                if (trackWidth > 0) {
                    val node = me.source as Node
                    dragContextX = node.translateX - me.sceneX
                    dragStart = localToParent(me.x, me.y)
                    me.consume()
                }
            }

            setOnMouseDragged { me ->
                val node = me.source as Node
                this.translateXProperty().unbind()
                node.translateX = dragContextX + me.sceneX
            }

            setOnMouseReleased { me ->
                val trackWidth = this.width
                if (trackWidth > 0.0) {
                    val cur: Point2D = localToParent(me.x, me.y)
                    if (dragStart == null) {
                        // we're getting dragged without getting a mouse press
                        dragStart = localToParent(me.x, me.y)
                    }
                    val deltaPos = cur.x - dragStart!!.x
                    onWaveformDragReleasedProperty.get()?.invoke(deltaPos)
                    dragStart = localToParent(me.x, me.y)
                    me.consume()
                    bindTranslateX() // rebind when done
                }
            }

            setOnKeyPressed {
                val speed = if (it.isControlDown) SeekSpeed.FAST else SeekSpeed.NORMAL
                when (it.code) {
                    KeyCode.LEFT -> {
                        onRewindProperty.value?.invoke(speed) { resume ->
                            resumeAfterScroll = resume
                        }
                        it.consume()
                    }
                    KeyCode.RIGHT -> {
                        onFastForwardProperty.value?.invoke(speed) { resume ->
                            resumeAfterScroll = resume
                        }
                        it.consume()
                    }
                }
            }
            setOnKeyReleased {
                when (it.code) {
                    KeyCode.LEFT, KeyCode.RIGHT -> {
                        if (resumeAfterScroll) {
                            resumeAfterScroll = false
                            onToggleMediaProperty.value?.invoke()
                        }
                        it.consume()
                    }
                    KeyCode.ENTER, KeyCode.SPACE -> {
                        onToggleMediaProperty.value?.invoke()
                        it.consume()
                    }
                }
            }
        }
    }

    private fun bindTranslateX() {
        this.translateXProperty().bind(
            framePositionProperty
                .negate()
                .plus(
                    this@WaveformFrame.widthProperty().divide(2.0)
                )
        )
    }

    fun addImage(image: Image) {
        imageHolder?.add(
            imageview(image) {
                fitToHeight(imageRegion)
            }
        )
    }

    fun addHighlights(highlights: List<MarkerHighlightState>) {
        highlights.forEach {
            highlightHolder.add(
                Rectangle().apply {
                    managedProperty().set(false)
                    heightProperty().bind(highlightHolder.heightProperty())
                    widthProperty().bind(it.width)
                    translateXProperty().bind(it.translate)
                    visibleProperty().bind(it.visibility)
                    it.styleClass.onChangeAndDoNow {
                        styleClass.setAll(it)
                    }
                }
            )
        }
    }

    fun clearHighlights() {
        highlightHolder.children.removeIf { it is Rectangle }
    }

    fun freeImages() {
        imageHolder?.getChildList()?.clear()
    }
}
