package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import tornadofx.*

class PluginOverlay: StackPane() {
    val iconProperty = SimpleObjectProperty<Node>(MaterialIconView(MaterialIcon.ACCESSIBLE))
    var icon by iconProperty

    var graphic : Node = StackPane()

    val visibleProperty = SimpleBooleanProperty(false)
    var overlayVisible by visibleProperty

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
        isVisible = visibleProperty.value
    }
}

fun Pane.pluginOverlay(init: PluginOverlay.()-> Unit = {} ): PluginOverlay {
    val pluginOverlay = PluginOverlay()
    pluginOverlay.init()
    add(pluginOverlay)
    return pluginOverlay

}