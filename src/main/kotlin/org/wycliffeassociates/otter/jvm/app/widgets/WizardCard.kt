package org.wycliffeassociates.otter.jvm.app.widgets

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

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