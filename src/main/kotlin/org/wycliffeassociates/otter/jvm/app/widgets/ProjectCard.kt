package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import org.wycliffeassociates.otter.common.data.model.Project
import tornadofx.*

class ProjectCard(project : Project, openProject: ()-> Unit) : Fragment(){
    override val root = vbox(20) {
        alignment = Pos.CENTER
        label(project.book.toString())
        label(project.targetLanguage.toString())
        button ("LOAD") {
            action {
                openProject()
            }
        }
    }
}
