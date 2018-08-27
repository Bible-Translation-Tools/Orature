package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Project
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ProjectCard(project: Project) : VBox() {

    val loadProjectButton = button("LOAD")
    init {
        with(root) {

            vbox(20) {
                alignment = Pos.CENTER
                label(project.book.toString())
                label(project.targetLanguage.toString())
                add(loadProjectButton)
            }
        }
}
}
