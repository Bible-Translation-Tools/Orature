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
package org.wycliffeassociates.otter.jvm.controls.narration

import javafx.beans.property.*
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.recorder.Drawable
import tornadofx.*

class ActiveRecordingCard : HBox() {
    val verseLabelProperty = SimpleStringProperty()

    val waveformDrawableProperty = SimpleObjectProperty<Drawable>()
    val volumebarDrawableProperty = SimpleObjectProperty<Drawable>()

    private val waveformWidth = ReadOnlyDoubleWrapper()
    val waveformWidthProperty = waveformWidth.readOnlyProperty
    private val waveformHeight = ReadOnlyDoubleWrapper()
    val waveformHeightProperty = waveformHeight.readOnlyProperty

    val isRecordingProperty = SimpleBooleanProperty()

    init {
        styleClass.setAll("narration-record__active-card")

        vbox {
            hbox {
                addClass("narration-record__verse-controls")
                vgrow = Priority.SOMETIMES

                label {
                    addClass("narration-record__verse-text")

                    graphic = FontIcon(MaterialDesign.MDI_BOOKMARK_OUTLINE)
                    textProperty().bind(verseLabelProperty)
                }

                region {
                    hgrow = Priority.ALWAYS
                }
            }

            hbox {
                addClass("narration-record__waveform")
                vgrow = Priority.ALWAYS

                add(CanvasFragment().apply {
                    hgrow = Priority.ALWAYS

                    isDrawingProperty.bind(isRecordingProperty)
                    drawableProperty.bind(waveformDrawableProperty)

                    waveformWidth.bind(widthProperty())
                    waveformHeight.bind(heightProperty())
                })
            }
        }

        stackpane {
            addClass("narration-record__volume-bar")

            add(CanvasFragment().apply {
                hgrow = Priority.ALWAYS

                isDrawingProperty.bind(isRecordingProperty)
                drawableProperty.bind(volumebarDrawableProperty)

                maxHeightProperty().bind(waveformHeightProperty)
            })
        }
    }
}

fun EventTarget.activerecordingcard(
    spacing: Number? = null,
    alignment: Pos? = null,
    op: ActiveRecordingCard.() -> Unit = {}
): ActiveRecordingCard {
    val card = ActiveRecordingCard()
    if (alignment != null) card.alignment = alignment
    if (spacing != null) card.spacing = spacing.toDouble()
    return opcr(this, card, op)
}
