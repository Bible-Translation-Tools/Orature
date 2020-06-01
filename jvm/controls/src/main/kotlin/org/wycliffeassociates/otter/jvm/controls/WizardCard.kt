package org.wycliffeassociates.otter.jvm.controls

import com.jfoenix.controls.JFXButton
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.*
import tornadofx.*

class WizardCard : VBox() {
    val textProperty = SimpleStringProperty()
    var text by textProperty

    val imageProperty = SimpleObjectProperty<Node>(VBox())
    var image by imageProperty

    val buttonTextProperty = SimpleStringProperty()
    var buttonText by buttonTextProperty

    var cardButton: Button
    var graphicContainer: Node = StackPane()

    init {
        spacing = 10.0
        graphicContainer = stackpane {
            vgrow = Priority.ALWAYS
        }
        label(textProperty) {
            alignment = Pos.CENTER
            useMaxWidth = true
            maxWidth = Double.MAX_VALUE
            textFill = c("#CC4141")
        }
        cardButton = JFXButton()
        add(cardButton)
    }
}

fun Pane.wizardcard(init: WizardCard.() -> Unit): WizardCard {
    val wc = WizardCard()
    wc.init()
    add(wc)
    return wc
}