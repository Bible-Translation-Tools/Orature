package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.fragments

import javafx.geometry.Insets
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
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
                    viewModel.bookList.onChange {
                        clear()
                        viewModel.bookList.forEach {
                            projectcard(it) {
                                addClass(AppStyles.projectCard)
                                graphicContainer.apply {
                                    addClass(AppStyles.projectGraphicContainer)
                                    add(MaterialIconView(MaterialIcon.IMAGE, "75px"))
                                }
                                cardButton.apply {
                                    text = messages["create"]
                                    action {
                                        viewModel.selectedBookProperty.setValue(it)
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
        viewModel.createProject()
        workspace.dock<ProjectHomeView>()
    }
}
