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

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Node
import javafx.scene.layout.HBox
import tornadofx.*

class LanguageCardCell : HBox() {

    val iconProperty = SimpleObjectProperty<Node>()
    val languageNameProperty = SimpleStringProperty()
    val languageSlugProperty = SimpleStringProperty()

    init {
        styleClass.setAll("language-card-cell")

        label {
            addClass("language-card-cell__icon")
            graphicProperty().bind(iconProperty)
        }

        vbox {
            addClass("language-card-cell__title")
            label(languageNameProperty).apply {
                addClass("language-card-cell__name")
            }
            label(languageSlugProperty).apply {
                addClass("language-card-cell__slug")
            }
        }
    }
}
