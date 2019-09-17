package org.wycliffeassociates.otter.jvm.app.widgets.resourcecard.view

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