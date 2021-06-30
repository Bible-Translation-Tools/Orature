/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.controls.card

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.effect.GaussianBlur
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*
import java.io.File

class InnerCard(val cardGraphic: Node? = null) : VBox() {

    val graphicPathProperty = SimpleObjectProperty<File>()

    val titleProperty = SimpleStringProperty()
    var title by titleProperty

    val bodyTextProperty = SimpleStringProperty()
    var bodyText by bodyTextProperty

    val majorLabelProperty = SimpleStringProperty()
    var majorLabel by majorLabelProperty

    val minorLabelProperty = SimpleStringProperty()
    var minorLabel by minorLabelProperty

    val showProgressProperty = SimpleBooleanProperty(false)
    var showProgress by showProgressProperty

    val progressProperty = SimpleDoubleProperty(0.0)
    var progress by progressProperty

    val selectedExistsProperty = SimpleBooleanProperty(false)
    var selectedExists by selectedExistsProperty

    init {
        importStylesheet<DefaultStyles>()
        graphicPathProperty.onChange {
            updateGraphic(it)
        }
        addClass(DefaultStyles.defaultInnerCard)
        stackpane {
            if (cardGraphic != null) {
                add(cardGraphic).apply {
                    style {
                        backgroundColor += Color.LIGHTGRAY
                    }
                }
            }
            vbox {
                alignment = Pos.BOTTOM_CENTER
                stackpane {
                    ellipse {
                        centerX = 0.0
                        centerY = 0.0
                        radiusX = 50.0
                        radiusY = 10.0
                        fill = Color.WHITE
                        effect = GaussianBlur(15.0)
                        visibleProperty().bind(titleProperty.booleanBinding { it != null })
                    }
                    label(titleProperty) {
                        addClass(DefaultStyles.defaultTitle)
                    }
                }
                stackpane {
                    ellipse {
                        centerX = 50.0
                        centerY = 0.0
                        radiusX = 20.0
                        radiusY = 20.0
                        fill = Color.WHITE
                        effect = GaussianBlur(15.0)
                        visibleProperty().bind(bodyTextProperty.booleanBinding { it != null })
                    }
                    label(bodyTextProperty) {
                        addClass(DefaultStyles.defaultBody)
                    }
                }
                label(majorLabelProperty) {
                    addClass(DefaultStyles.defaultMajorLabel)
                    visibleWhen { majorLabelProperty.isNotEmpty }
                }
                label(minorLabelProperty) {
                    graphic = DefaultStyles.checkCircle("25px").apply {
                        fill = DefaultStyles.green()
                    }
                    graphic.managedProperty().bind(selectedExistsProperty.booleanBinding { it != false })
                    graphic.visibleProperty().bind(selectedExistsProperty.booleanBinding { it != false })
                    addClass(DefaultStyles.defaultMinorLabel)
                    visibleWhen { minorLabelProperty.isNotEmpty }
                }
                progressbar(progressProperty) {
                    addClass(DefaultStyles.defaultCardProgressBar)
                    managedProperty().bind(showProgressProperty.booleanBinding { it != false })
                    visibleProperty().bind(showProgressProperty.booleanBinding { it != false })
                    toggleClass(DefaultStyles.completedProgress, progressProperty
                        .booleanBinding { it != null && 0.9999 <= it.toDouble() })
                }
            }
        }
    }

    private fun updateGraphic(graphic: File?) {
        graphic?.let {
            val image = Image(graphic.inputStream())
            val iv = ImageView(image)
            iv.fitWidthProperty().value = 150.0
            iv.fitHeightProperty().value = 150.0
            (this@InnerCard.children.first() as StackPane).children.removeAt(0)
            (this@InnerCard.children.first() as StackPane).children.add(0, iv)
        } ?: run {
            resetGraphicToDefault()
        }
    }

    private fun resetGraphicToDefault() {
        cardGraphic?.let {
            (this@InnerCard.children.first() as StackPane).children.removeAt(0)
            (this@InnerCard.children.first() as StackPane).children.add(0, it)
        }
    }
}

fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
    val ic = InnerCard(cardGraphic)
    ic.init()
    return ic
}