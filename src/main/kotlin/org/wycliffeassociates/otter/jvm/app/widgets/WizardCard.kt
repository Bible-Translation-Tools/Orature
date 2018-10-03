package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class WizardCard: VBox() {
    val textProperty = SimpleStringProperty()
    var text by textProperty

    val imageProperty = SimpleObjectProperty<Node>(VBox())
    var image by imageProperty

    val selectedProperty = SimpleBooleanProperty()
    var selected by selectedProperty

    init {
        spacing = 30.0
        importStylesheet<WidgetsStyles>()
        with(root) {
            addClass(WidgetsStyles.wizardCard)
            alignment = Pos.CENTER

            label(textProperty){
                textFill = c("#FFFF")
            }
            button("Select") {
                addClass(WidgetsStyles.wizardCardButton)
            }

        }
    }
}

fun Pane.wizardcard( init: WizardCard.() -> Unit): WizardCard {
    val wc = WizardCard()
    wc.init()
    add(wc)
    return wc
}