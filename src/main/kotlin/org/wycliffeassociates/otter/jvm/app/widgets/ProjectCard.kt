package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Project
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ProjectCard(project: Project) : VBox() {
    val loadButton = Button("Load")

    init {
        with(root) {
            vbox(20) {
                alignment = Pos.CENTER
                label(project.book.titleKey)
                label(project.targetLanguage.name)
                add(loadButton)
            }
        }
    }
}

fun Pane.projectcard(project: Project, init: ProjectCard.() -> Unit = {}): ProjectCard {
    val projectCard = ProjectCard(project)
    projectCard.init()
    add(projectCard)
    return projectCard
}
