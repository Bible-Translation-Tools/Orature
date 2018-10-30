package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*

class ProgressOverlay : StackPane() {

    var iconProperty = SimpleObjectProperty<Node>(VBox())
    var icon by iconProperty

    var textProperty = SimpleStringProperty()
    var text by textProperty

    init {
        style {
            alignment = Pos.CENTER
            backgroundColor += Color.BLACK
                    .deriveColor(0.0, 0.0, 0.0, 0.5)
        }

        hbox {
            alignment = Pos.CENTER
            add(iconProperty.value)
            iconProperty.onChange {
                clear()
                stackpane {
                    add(iconProperty.value)
                }
            }
        }
       // vbox {
         //   alignment = Pos.CENTER
           // style {
             //   prefWidth = 250.px
               // prefHeight = 250.px
            //}
            progressindicator {
                style {
                    maxWidth = 125.px
                    maxHeight = 125.px
                    progressColor = Color.WHITE
                }
            }
            label(textProperty)
        //}


    }
}

fun Pane.progressOverlay(init: ProgressOverlay.() -> Unit = {}): ProgressOverlay {
    val progressOverlay = ProgressOverlay()
    progressOverlay.init()
    add(progressOverlay)
    return progressOverlay
}