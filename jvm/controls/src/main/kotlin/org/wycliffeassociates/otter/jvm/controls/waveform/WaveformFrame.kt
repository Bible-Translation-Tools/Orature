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
package org.wycliffeassociates.otter.jvm.controls.waveform

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
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.utils.images.fitToHeight

import tornadofx.*

class WaveformFrame(
    topTrack: Node? = null,
    bottomTrack: Node? = null
) : StackPane() {

    private val onWaveformClickedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    private val onWaveformDragReleasedProperty = SimpleObjectProperty<(pixel: Double) -> Unit>()
    private val onRewindProperty = SimpleObjectProperty<(ScrollSpeed) -> Unit>()
    private val onFastForwardProperty = SimpleObjectProperty<(ScrollSpeed) -> Unit>()
    private val onToggleMediaProperty = SimpleObjectProperty<() -> Unit>()
    private val onResumeMediaProperty = SimpleObjectProperty<() -> Unit>()
    private val onSeekPreviousProperty = SimpleObjectProperty<() -> Unit>()
    private val onSeekNextProperty = SimpleObjectProperty<() -> Unit>()

    val framePositionProperty = SimpleDoubleProperty(0.0)

    fun onWaveformClicked(op: () -> Unit) {
        onWaveformClickedProperty.set(EventHandler { op.invoke() })
    }

    fun onWaveformDragReleased(op: (pixel: Double) -> Unit) {
        onWaveformDragReleasedProperty.set(op)
    }

    fun onRewind(op: (ScrollSpeed) -> Unit) {
        onRewindProperty.set(op)
    }

    fun onFastForward(op: (ScrollSpeed) -> Unit) {
        onFastForwardProperty.set(op)
    }

    fun onToggleMedia(op: () -> Unit) {
        onToggleMediaProperty.set(op)
    }

    fun onResumeMedia(op: () -> Unit) {
        onResumeMediaProperty.set(op)
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

    init {
        addClass("vm-waveform-frame")

        fitToParentSize()
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS

        with(this) {
            bindTranslateX()

            region {
                imageRegion = this
                stackpane {
                    highlightHolder = this
                    styleClass.add("scrolling-waveform-frame__center")
                    alignment = Pos.CENTER

                    hbox {
                        imageHolder = this@hbox
                    }

                    borderpane {
                        top {
                            region {
                                styleClass.add("scrolling-waveform-frame__top-track")
                            }
                        }
                        bottom {
                            region {
                                styleClass.add("scrolling-waveform-frame__bottom-track")
                                bottomTrack?.let {
                                    add(it)
                                }
                            }
                        }
                    }

                    topTrack?.let {
                        add(it.apply {
                            val me = (it as MarkerTrackControl)
                            me.onSeekPreviousProperty.bind(this@WaveformFrame.onSeekPreviousProperty)
                            me.onSeekNextProperty.bind(this@WaveformFrame.onSeekNextProperty)
                        })
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
                val speed = if (it.isControlDown) ScrollSpeed.FAST else ScrollSpeed.NORMAL
                when (it.code) {
                    KeyCode.LEFT -> {
                        onRewindProperty.value?.invoke(speed)
                        it.consume()
                    }
                    KeyCode.RIGHT -> {
                        onFastForwardProperty.value?.invoke(speed)
                        it.consume()
                    }
                }
            }
            setOnKeyReleased {
                when (it.code) {
                    KeyCode.LEFT, KeyCode.RIGHT -> {
                        onResumeMediaProperty.value?.invoke()
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

    fun freeImages() {
        imageHolder?.getChildList()?.clear()
    }
}
