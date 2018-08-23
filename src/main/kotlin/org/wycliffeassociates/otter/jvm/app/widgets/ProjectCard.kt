package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Project
import org.wycliffeassociates.otter.common.ui.widgets.IProjectCard
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ProjectCard(project : Project, openProject: () -> Unit) : VBox(), IProjectCard{
     init {
         with(root) {
             vbox(20) {
                 alignment = Pos.CENTER
                 label(project.book.toString())
                 label(project.targetLanguage.toString())
                 button("LOAD") {
                     action {
                         openProject()
                     }
                 }
             }
         }
     }

    override fun openProject() {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        openProject()
    }
}
