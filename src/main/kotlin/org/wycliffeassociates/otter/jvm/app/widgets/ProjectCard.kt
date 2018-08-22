package org.wycliffeassociates.otter.jvm.app.widgets

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import tornadofx.*

class ProjectCard(value : String, openProject: ()-> Unit) : Fragment(){
    override val root = vbox(20) {
        alignment = Pos.CENTER
        label(value)
        label("English")
        button ("LOAD") {
            action {
                openProject()
            }
        }
    }
}
