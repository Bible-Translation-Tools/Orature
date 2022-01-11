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
package org.wycliffeassociates.otter.jvm.controls.navigation

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.controls.card.InnerCard
import tornadofx.*

class NavBox(mainLabel: String? = null, graphic: Node? = null) : StackPane() {

    val layerList = observableListOf<Node>()

    fun innercard(cardGraphic: Node? = null, init: InnerCard.() -> Unit = {}): InnerCard {
        val ic = InnerCard(cardGraphic)
        ic.init()
        ic.addClass(ProjectNavStyles.navBoxInnercard)
        addLayer(ic)
        return ic
    }

    fun addLayer(nextLayer: Node) {
        // add the layer that will be added on top the empty nav box
        layerList.setAll(nextLayer)
    }

    init {

        style {
            prefWidth = 150.px
            prefHeight = 140.px
            backgroundColor += c("#E6E8E9")
            borderWidth += box(2.0.px)
            borderColor += box(Color.GRAY)
            borderRadius += box(5.0.px)
            backgroundRadius += box(5.0.px)
            maxWidth = 180.0.px
            cursor = Cursor.HAND
        }
        vbox(10) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            alignment = Pos.CENTER
            if (graphic != null) {
                add(graphic)
            }

            if (mainLabel != null) {
                label(mainLabel)
            }
        }

        layerList.onChange {
            it.list.forEach {
                add(it)
            }
        }
    }
}
