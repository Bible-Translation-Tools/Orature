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
package org.wycliffeassociates.otter.jvm.controls.dialog

import io.github.palexdev.materialfx.controls.MFXProgressBar
import javafx.beans.property.SimpleStringProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class LoadingModal : OtterDialog() {

    val messageProperty = SimpleStringProperty()

    private val content = VBox().apply {
        addClass("confirm-dialog", "progress-dialog")

        vbox {
            addClass("confirm-dialog__body")
            vgrow = Priority.ALWAYS
            label(messageProperty) {
                addClass("confirm-dialog__message", "normal-text")
            }
            add(
                MFXProgressBar().apply {
                    prefWidthProperty().bind(this@vbox.widthProperty())
                }
            )
        }

    }

    init {
        setContent(content)
    }
}