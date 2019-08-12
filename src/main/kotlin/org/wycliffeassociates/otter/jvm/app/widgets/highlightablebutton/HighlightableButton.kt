package org.wycliffeassociates.otter.jvm.app.widgets.highlightablebutton

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.paint.Color
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
        isDisableVisualFocus = true

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
            if(hasBorder) {
                borderColor += box(highlightColor)
            }
        }
        (graphic as? MaterialIconView)?.apply {
            fill = contentFillColor
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