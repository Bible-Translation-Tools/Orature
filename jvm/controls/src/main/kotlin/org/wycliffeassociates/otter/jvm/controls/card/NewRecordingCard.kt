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

import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign

class NewRecordingCard(
    val labelText: String,
    val buttonText: String,
    val action: () -> Unit
) : VBox() {
    init {
        with(this) {
            styleClass.addAll("card--scripture-take", "card--take--new")

            children.addAll(
                Label(labelText).apply {
                    styleClass.add("card--take--new-label")
                },
                Button(buttonText).apply {
                    styleClass.addAll(
                        "btn",
                        "card--take--new-button"
                    )
                    graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                    setOnAction { action() }
                }
            )
        }
    }
}
