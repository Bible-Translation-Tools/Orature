package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.Node
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.widgets.WidgetsStyles.Companion.activityPanelButton
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ActivityPanel(graphicLeft: Node, graphicMiddleLeft: Node,
                    graphicMiddleRight: Node, graphicRight: Node,
                    buttonLeftColor: Color, buttonMidLeftColor: Color,
                    buttonMidRightColor: Color, buttonRightColor: Color) : HBox() {
    val buttonLeft = button("", graphicLeft){
        importStylesheet(WidgetsStyles::class)
        style{
            backgroundColor += buttonLeftColor
        }
        addClass(WidgetsStyles.activityPanelButton)
    }
    val buttonCenterLeft = button("", graphicMiddleLeft){
        style{
            backgroundColor += buttonMidLeftColor
        }
        addClass(WidgetsStyles.activityPanelButton)
    }
    val buttonCenterRight = button("", graphicMiddleRight){
        style{
            backgroundColor += buttonMidRightColor
        }
        addClass(WidgetsStyles.activityPanelButton)
    }
    val buttonRight = button("", graphicRight) {
        style {
            backgroundColor += buttonRightColor
        }
        addClass(WidgetsStyles.activityPanelButton)
    }
    init {
        with(root) {
            spacing = 10.0
        }
    }
}
