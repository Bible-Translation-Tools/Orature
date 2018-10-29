package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class ProgressOverlay : StackPane() {

    var iconProperty = SimpleObjectProperty<Node>(MaterialIconView(MaterialIcon.MIC_NONE, "60px"))
    var icon by iconProperty

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
        progressindicator {
            style {
                maxWidth = 125.px
                maxHeight = 125.px
                progressColor = Color.WHITE
            }
        }


    }
}

fun Pane.progressOverlay(init: ProgressOverlay.() -> Unit = {}): ProgressOverlay {
    val progressOverlay = ProgressOverlay()
    progressOverlay.init()
    add(progressOverlay)
    return progressOverlay
}