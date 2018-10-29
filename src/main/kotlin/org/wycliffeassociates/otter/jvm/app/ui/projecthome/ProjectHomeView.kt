package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.projectcard
import tornadofx.*

class ProjectHomeView : View() {

    init {
        importStylesheet<AppStyles>()
    }

    val viewModel: ProjectHomeViewModel by inject()
    override val root = anchorpane {
        style {
            setPrefSize(1200.0, 800.0)
        }
        scrollpane {
            isFitToHeight = true
            isFitToWidth = true
            anchorpaneConstraints {
                topAnchor = 0
                bottomAnchor = 0
                leftAnchor = 0
                rightAnchor = 0
            }
            content = flowpane {
                vgap = 16.0
                hgap = 16.0
                alignment = Pos.TOP_LEFT
                // Add larger padding on bottom to keep FAB from blocking last row cards
                padding = Insets(10.0, 10.0, 95.0, 10.0)
                bindChildren(viewModel.allProjects) {
                    hbox {
                        projectcard(it) {
                            addClass(AppStyles.projectCard)
                            titleLabel.addClass(AppStyles.projectCardTitle)
                            languageLabel.addClass(AppStyles.projectCardLanguage)
                            cardButton.apply {
                                text = messages["loadProject"]
                                action {
                                    viewModel.openProject(it)
                                }
                            }
                            graphicContainer.apply {
                                addClass(AppStyles.projectGraphicContainer)
                                add(MaterialIconView(MaterialIcon.IMAGE, "75px"))
                            }
                        }
                    }
                }
            }
        }
        add(JFXButton("", MaterialIconView(MaterialIcon.ADD, "25px")).apply {
            addClass(AppStyles.addProjectButton)
            anchorpaneConstraints {
                bottomAnchor = 25
                rightAnchor = 25
            }
            action {
                viewModel.createProject()
            }
        })

    }

    override fun onDock() {
        viewModel.getAllProjects()
    }

}