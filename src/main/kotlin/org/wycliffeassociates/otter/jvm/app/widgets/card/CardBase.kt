package org.wycliffeassociates.otter.jvm.app.widgets.card

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class CardBase : VBox() {
    init {
        importStylesheet<DefaultStyles>()
        vgrow = Priority.ALWAYS
        alignment = Pos.TOP_CENTER
        add(
            vbox {
                vgrow = Priority.ALWAYS
                addClass(DefaultStyles.defaultBaseTop)
            }
        )
        addClass(DefaultStyles.baseBottom)
        // card top half = vbox?
        // card bottom half color is just the color of the card
    }
}