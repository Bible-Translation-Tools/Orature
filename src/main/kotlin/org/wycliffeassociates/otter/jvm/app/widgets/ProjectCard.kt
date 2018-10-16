package org.wycliffeassociates.otter.jvm.app.widgets

import com.jfoenix.controls.JFXButton
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import tornadofx.*

class ProjectCard(project: ProjectCollection) : VBox() {
    var cardButton: Button
    var graphicContainer: Node = StackPane()

    init {
        label(project.titleKey) {
            alignment = Pos.CENTER
            useMaxWidth = true
            maxWidth = Double.MAX_VALUE
        }
        graphicContainer = stackpane {
            vgrow = Priority.ALWAYS
        }
        cardButton = JFXButton()
        add(cardButton)
    }
}

fun Pane.projectcard(project: ProjectCollection, init: ProjectCard.() -> Unit = {}): ProjectCard {

    val projectCard = ProjectCard(project)
    projectCard.init()
    add(projectCard)
    return projectCard
}
