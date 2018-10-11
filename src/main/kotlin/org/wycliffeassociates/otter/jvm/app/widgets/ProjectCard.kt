package org.wycliffeassociates.otter.jvm.app.widgets

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Collection
import tornadofx.*
import tornadofx.Stylesheet.Companion.root

class ProjectCard(project: Collection) : VBox() {
    val loadButton = Button()

    init {
        with(root) {
            vbox(20) {
                alignment = Pos.CENTER
                label(project.titleKey)
                label(project.resourceContainer.language.name)
                add(loadButton)
            }
        }
    }
}

fun Pane.projectcard(project: Collection, init: ProjectCard.() -> Unit = {}): ProjectCard {
    val projectCard = ProjectCard(project)
    projectCard.init()
    add(projectCard)
    return projectCard
}
