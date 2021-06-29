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
package org.wycliffeassociates.otter.jvm.controls.styles

import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.text.FontWeight
import tornadofx.*

class HighlightableButtonStyles : Stylesheet() {

    companion object {
        val hButton by cssclass()
    }

    init {
        hButton {
            alignment = Pos.CENTER
            maxHeight = 40.px
            borderRadius += box(5.0.px)
            backgroundRadius += box(5.0.px)
            cursor = Cursor.HAND
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
        }
    }
}
