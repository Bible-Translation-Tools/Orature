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
package org.wycliffeassociates.otter.jvm.controls.demo.ui.fragments

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import tornadofx.*

class ButtonFragment : Fragment() {
    override val root = stackpane {
        vbox {
            spacing = 10.0
            alignment = Pos.CENTER

            button("Primary") {
                addClass("btn", "btn--primary")
            }
            button("Secondary") {
                addClass("btn", "btn--secondary")
            }
            button("Borderless") {
                addClass("btn", "btn--secondary", "btn--borderless")
            }
            button("Call to Action") {
                addClass("btn", "btn--cta")
            }
            button("With Icon") {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ACCOUNT)
            }
            button("Disabled") {
                addClass("btn", "btn--primary")
                graphic = FontIcon(MaterialDesign.MDI_ACCOUNT)
                isDisable = true
            }
        }
    }
}
