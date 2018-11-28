package org.wycliffeassociates.otter.jvm.app.widgets

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.model.Collection
import tornadofx.*

class ProjectCard(project: Collection) : VBox() {
    var cardButton: Button
    var graphicContainer: Node = StackPane()

    var showLanguage: Boolean by property(true)
    val showLanguageProperty = getProperty(ProjectCard::showLanguage)

    val titleLabel: Label
    val languageLabel: Label

    init {
        titleLabel = label(project.titleKey) {
            alignment = Pos.CENTER
            useMaxWidth = true
            maxWidth = Double.MAX_VALUE
        }
        languageLabel = label(project.resourceContainer?.language?.name ?: "")
        hbox(10.0) {
            alignment = Pos.CENTER
            add(MaterialIconView(MaterialIcon.RECORD_VOICE_OVER, "16px").apply {
                fillProperty().bind(languageLabel.textFillProperty())
            })
            add(languageLabel)
            visibleProperty().bind(showLanguageProperty)
            managedProperty().bind(visibleProperty())
        }

        graphicContainer = stackpane {
            vgrow = Priority.ALWAYS
        }
        cardButton = JFXButton()
        add(cardButton)
    }
}

fun Pane.projectcard(project: Collection, init: ProjectCard.() -> Unit = {}): ProjectCard {
    val projectCard = ProjectCard(project)
    projectCard.init()
    add(projectCard)
    return projectCard
}
