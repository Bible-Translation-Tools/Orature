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
package org.wycliffeassociates.otter.jvm.controls.card

import com.jfoenix.controls.JFXButton
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import tornadofx.*

class CardFront : StackPane() {

    val childrenList = observableListOf<Node>()

    val defaultFill = c("#CC4141")
    val forwardArrow = FontIcon("gmi-arrow-forward").apply { iconSize = 20 }

    val isActiveProperty = SimpleBooleanProperty(false)
    var isActive by isActiveProperty

    val isCompleteProperty = SimpleBooleanProperty(false)
    var isComplete by isCompleteProperty

    fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
        val ic = InnerCard(cardGraphic)
        ic.init()
        addLayer(ic)
        return ic
    }

    fun cardbutton(init: JFXButton.() -> Unit = {}): JFXButton {
        val bttn = JFXButton()
        bttn.init()
        addLayer(bttn)
        return bttn
    }

    fun addLayer(layer: Node) {
        val tempVbox: VBox = vbox(10) {
            alignment = Pos.CENTER
            style {
                padding = box(2.0.px)
            }
            // add all existing children into the temp Vbox
            childrenList.forEach {
                add(it)
            }
            // add the new layer that will be at bottom of vbox
            add(layer)
        }

        childrenList.setAll(tempVbox)
    }

    init {
        importStylesheet<DefaultStyles>()
        forwardArrow.iconColor = defaultFill
        alignment = Pos.TOP_CENTER
        // the colored top half of the card
        vbox {
            vgrow = Priority.ALWAYS
            addClass(DefaultStyles.defaultBaseTop)
            toggleClass(DefaultStyles.activeBaseTop, isActiveProperty)
            toggleClass(DefaultStyles.completeBaseTop, isCompleteProperty)
        }

        vbox(10) {
            childrenList.onChange {
                it.list.forEach {
                    add(it)
                }
            }
        }
    }
}
