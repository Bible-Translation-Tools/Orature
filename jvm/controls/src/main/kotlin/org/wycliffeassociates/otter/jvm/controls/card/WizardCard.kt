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

import com.jfoenix.controls.JFXButton
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.*
import tornadofx.*

class WizardCard : VBox() {
    val textProperty = SimpleStringProperty()
    var text by textProperty

    val imageProperty = SimpleObjectProperty<Node>(VBox())
    var image by imageProperty

    val buttonTextProperty = SimpleStringProperty()
    var buttonText by buttonTextProperty

    var cardButton: Button
    var graphicContainer: Node = StackPane()

    init {
        spacing = 10.0
        graphicContainer = stackpane {
            vgrow = Priority.ALWAYS
        }
        label(textProperty) {
            alignment = Pos.CENTER
            useMaxWidth = true
            maxWidth = Double.MAX_VALUE
            textFill = c("#CC4141")
        }
        cardButton = JFXButton()
        add(cardButton)
    }
}

fun Pane.wizardcard(init: WizardCard.() -> Unit): WizardCard {
    val wc = WizardCard()
    wc.init()
    add(wc)
    return wc
}
