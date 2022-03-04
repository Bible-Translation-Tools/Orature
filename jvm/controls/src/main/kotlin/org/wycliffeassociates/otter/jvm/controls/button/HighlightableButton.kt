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
package org.wycliffeassociates.otter.jvm.controls.button

import com.jfoenix.controls.JFXButton
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.styles.HighlightableButtonStyles
import tornadofx.*

class HighlightableButton : JFXButton() {

    var isHighlightedProperty: SimpleBooleanProperty = SimpleBooleanProperty(false)
    var isHighlighted: Boolean by isHighlightedProperty
    var highlightColorProperty = SimpleObjectProperty<Color>(Color.BLACK)
    var highlightColor: Color by highlightColorProperty
    var secondaryColor: Color = Color.WHITE
    var hasBorder: Boolean = false

    init {
        importStylesheet<HighlightableButtonStyles>()

        addClass(HighlightableButtonStyles.hButton)

        isHighlightedProperty.onChange { applyColors() }
        hoverProperty().onChange { applyColors() }
        highlightColorProperty.onChange { applyColors() }
    }

    fun applyColors() {
        if (hoverProperty().get() || isHighlightedProperty.get()) {
            doApplyColors(highlightColor, secondaryColor)
        } else {
            doApplyColors(secondaryColor, highlightColor)
        }
    }

    private fun doApplyColors(bgColor: Color, contentFillColor: Color) {
        style {
            backgroundColor += bgColor
            if (hasBorder) {
                borderColor += box(highlightColor)
            }
        }
        (graphic as? FontIcon)?.apply {
            iconColor = contentFillColor
        }
        textFill = contentFillColor
    }
}

fun highlightablebutton(op: HighlightableButton.() -> Unit = {}): HighlightableButton {
    val btn = HighlightableButton()
    btn.op()
    btn.applyColors()
    return btn
}
