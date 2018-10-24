package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class PluginOverlay: StackPane() {

    var graphic : Node = StackPane()

    init {
        style {
            alignment = Pos.CENTER
            backgroundColor += Color.BLACK
                    .deriveColor(0.0, 0.0, 0.0, 0.5)
        }
        graphic = stackpane {
            vgrow = Priority.ALWAYS
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

fun Pane.pluginOverlay(init: PluginOverlay.()-> Unit = {} ): PluginOverlay {
    val pluginOverlay = PluginOverlay()
    pluginOverlay.init()
    add(pluginOverlay)
    return pluginOverlay

}