package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import com.jfoenix.controls.JFXButton
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.projectcard
import tornadofx.*

class ProjectHomeView : View() {

    init {
        importStylesheet<AppStyles>()
    }

    val viewModel: ProjectHomeViewModel by inject()
    override val root = borderpane {
        style {
            setPrefSize(1200.0, 800.0)
        }
        top = hbox {
            alignment = Pos.CENTER_RIGHT
            add(JFXButton(messages["refresh"], MaterialIconView(MaterialIcon.REFRESH, "25px")).apply {
                addClass(AppStyles.refreshButton)
                action {
                    viewModel.getAllProjects()
                }
            })
            style {
                padding = box(15.0.px)
            }
        }
        style {
            setPrefSize(1200.0, 800.0)
        }
        center {
            scrollpane {
                isFitToHeight = true
                isFitToWidth = true
                flowpane {
                    vgap = 16.0
                    hgap = 16.0
                    alignment = Pos.CENTER
                    padding = Insets(10.0)
                    bindChildren(viewModel.allProjects) {
                        hbox {
                            projectcard(it) {
                                addClass(AppStyles.projectCard)
                                cardButton.apply {
                                    text = messages["loadProject"]
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
        }

        bottom = hbox {
            style {
                padding = box(25.0.px)
            }
            alignment = Pos.BOTTOM_RIGHT
            val icon = MaterialIconView(MaterialIcon.ADD, "25px")
            add(JFXButton("", icon).apply {
                addClass(AppStyles.addProjectButton)
                action {
                    viewModel.createProject()
                }
            })
        }

    }

    override fun onDock() {
        viewModel.getAllProjects()
    }

}