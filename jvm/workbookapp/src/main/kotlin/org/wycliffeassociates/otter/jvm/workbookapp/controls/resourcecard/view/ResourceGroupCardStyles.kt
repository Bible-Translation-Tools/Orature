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
package org.wycliffeassociates.otter.jvm.workbookapp.controls.resourcecard.view

import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class ResourceGroupCardStyles : Stylesheet() {
    companion object {
        val resourceGroupCard by cssclass()
    }
    init {
        resourceGroupCard {
            spacing = 10.px // VBox spacing
            padding = box(15.px)
            backgroundColor += Color.WHITE
            effect = DropShadow(2.0, 2.0, 4.0, Color.LIGHTGRAY)
            backgroundRadius += box(5.px) // No border, so background needs to be rounded
            label {
                fontWeight = FontWeight.BOLD
            }
        }
    }
}