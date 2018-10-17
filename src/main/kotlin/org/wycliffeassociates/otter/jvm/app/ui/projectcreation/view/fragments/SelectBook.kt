package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.effect.DropShadow
import javafx.scene.paint.Color
import org.wycliffeassociates.otter.jvm.app.UIColorsObject.Colors
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel.ProjectCreationViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.ui.styles.AppStyles
import org.wycliffeassociates.otter.jvm.app.widgets.projectcard
import tornadofx.*


class SelectBook : View() {
    val viewModel: ProjectCreationViewModel by inject()

    override val root = borderpane {
        center {
            scrollpane {
                isFitToHeight = true
                isFitToWidth = true
                flowpane {
                    vgap = 16.0
                    hgap = 16.0
                    alignment = Pos.CENTER
                    padding = Insets(10.0)
                    bindChildren(viewModel.bookList) {
                        hbox {
                            projectcard(it) {
                                style {
                                    prefHeight = 250.0.px
                                    prefWidth = 232.0.px
                                    backgroundColor += c(Colors["base"])
                                    backgroundRadius += box(10.0.px)
                                    borderRadius += box(10.0.px)
                                    effect = DropShadow(4.0, 2.0, 4.0, Color.GRAY)
                                }
                                buttonText = messages["create"]
                                cardButton.apply {
                                    addClass(AppStyles.cardButton)
                                    style {
                                        effect = DropShadow(0.0, Color.TRANSPARENT)
                                        backgroundColor += c(Colors["primary"])
                                        textFill = c(Colors["base"])
                                    }
                                    action {
//                                        viewModel.selectedBookProperty.value = it
                                        onSave()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSave() {
       // super.onSave()
        viewModel.createProject()
        workspace.dock<ProjectHomeView>()
        //isComplete = true
        //close()
    }
}
