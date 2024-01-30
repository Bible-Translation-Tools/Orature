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
package org.wycliffeassociates.otter.jvm.controls.waveform

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Point2D
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.ColorAdjust
import javafx.scene.image.Image
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import org.wycliffeassociates.otter.common.data.ColorTheme
import org.wycliffeassociates.otter.jvm.controls.UIVersion
import org.wycliffeassociates.otter.jvm.controls.controllers.ScrollSpeed
import org.wycliffeassociates.otter.jvm.controls.marker.MarkerTrackControl

import tornadofx.*
import kotlin.math.min

const val WAVEFORM_MAX_HEIGHT = 500.0

class WaveformFrame(
    topTrack: Node? = null,
    bottomTrack: Node? = null
) : StackPane() {
    /**
     * Flag to determine if this reusable component follows the old or new design.
     */
    val uiVersionProperty = SimpleObjectProperty(UIVersion.ONE)
    val onWaveformClickedProperty = SimpleObjectProperty<EventHandler<ActionEvent>>()
    val onWaveformDragReleasedProperty = SimpleObjectProperty<(pixel: Double) -> Unit>()
    val onRewindProperty = SimpleObjectProperty<(ScrollSpeed) -> Unit>()
    val onFastForwardProperty = SimpleObjectProperty<(ScrollSpeed) -> Unit>()
    val onToggleMediaProperty = SimpleObjectProperty<() -> Unit>()
    val onResumeMediaProperty = SimpleObjectProperty<() -> Unit>()
    val onSeekPreviousProperty = SimpleObjectProperty<() -> Unit>()
    val onSeekNextProperty = SimpleObjectProperty<() -> Unit>()
    val onSeekProperty = SimpleObjectProperty<(Int) -> Unit>()

    val themeProperty = SimpleObjectProperty<ColorTheme>()
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

    private var imageHolder: HBox? = null
    private var dragContextX = 0.0
    private var dragStart: Point2D? = null
    private val waveformColorEffect = ColorAdjust()

    lateinit var imageRegion: Region

    lateinit var topTrackRegion: Region
    lateinit var bottomTrackRegion: Region

    init {
        addClass("vm-waveform-frame")

        with(this) {
            bindTranslateX()

            alignment = Pos.CENTER

            pane {
                imageRegion = this
                stackpane {
                    fitToParentHeight()
                    styleClass.add("scrolling-waveform-frame__center")
                    alignment = Pos.CENTER

                    themeProperty.onChange {
                        it?.let { theme ->
                            adjustWaveformColorByTheme(theme, waveformColorEffect)
                        }
                    }

                    /**
                     * Putting this in the middle of the borderpane below will result in one of the following errors:
                     *
                     * 1. The width of this container will push beyond the bounds of the window and push the app bar
                     * off screen.
                     *
                     * 2. The width of the marker track will not extend to the end of the waveform for longer recordings
                     */
                    hbox {
                        addClass("scrolling-waveform-frame__image-container")
                        alignment = Pos.CENTER
                        imageHolder = this@hbox
                    }

                    borderpane {
                        visibleWhen { uiVersionProperty.booleanBinding { it == UIVersion.ONE } }
                        managedWhen(visibleProperty())

                        top {
                            pane {
                                topTrackRegion = this
                                styleClass.add("scrolling-waveform-frame__top-track")
                            }
                        }
                        bottom {
                            pane {
                                bottomTrackRegion = this
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
                            me.onSeekProperty.bind(this@WaveformFrame.onSeekProperty)
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

                    else -> {}
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

                    else -> {}
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
                addClass("waveform-image")
                this.effect = waveformColorEffect
                // This is to adjust the height of the image to fit within the tracks
                if (uiVersionProperty.value == UIVersion.THREE) {
                    fitHeightProperty().bind(
                        imageRegion.heightProperty().doubleBinding {
                            it?.let { min(WAVEFORM_MAX_HEIGHT, it.toDouble()) } ?: image.height
                        }
                    )
                } else {
                    fitHeightProperty()
                        .bind(
                            imageRegion.heightProperty()
                                .minus(topTrackRegion.heightProperty())
                                .minus(bottomTrackRegion.heightProperty())
                        )
                }
            }
        )
    }

    fun freeImages() {
        imageHolder?.getChildList()?.clear()
    }
}
