package org.wycliffeassociates.otter.jvm.app.widgets.projectcard

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.resourcecontainer.entity.Project
import tornadofx.*

class ProjectCard(project: Collection) : AnchorPane() {
    var cardButton: Button by singleAssign()
    var deleteButton: Button by singleAssign()
    var graphicContainer: Node = StackPane()

    var showLanguage: Boolean by property(true)
    val showLanguageProperty = getProperty(ProjectCard::showLanguage)

    var titleLabel: Label by singleAssign()
    var languageLabel: Label by singleAssign()

    init {
        vbox(10) {
            anchorpaneConstraints {
                topAnchor = 0
                bottomAnchor = 0
                leftAnchor = 0
                rightAnchor = 0
            }
            titleLabel = label(project.titleKey) {
                alignment = Pos.CENTER
                useMaxWidth = true
                maxWidth = Double.MAX_VALUE
            }
            languageLabel = label(project.resourceContainer?.language?.name ?: "")
            hbox(10.0) {
                alignment = Pos.CENTER
                add(ProjectCardStyles.targetIcon("16px").apply {
                    fillProperty().bind(languageLabel.textFillProperty())
                })
                add(languageLabel)
                visibleProperty().bind(showLanguageProperty)
                managedProperty().bind(visibleProperty())
            }
            graphicContainer = stackpane {
                vgrow = Priority.ALWAYS
            }
            cardButton = JFXButton().apply {
                addClass(ProjectCardStyles.projectCardButton)
            }
            add(cardButton)
        }
        deleteButton = JFXButton().apply {
            graphic = ProjectCardStyles.deleteIcon("16px")
            addClass(ProjectCardStyles.deleteProjectButton)
            anchorpaneConstraints {
                topAnchor = 0
                rightAnchor = 0
            }
        }
        add(deleteButton)
    }
}

fun Pane.projectcard(project: Collection, init: ProjectCard.() -> Unit = {}): ProjectCard {
    val projectCard = ProjectCard(project)
    projectCard.init()
    add(projectCard)
    return projectCard
}
