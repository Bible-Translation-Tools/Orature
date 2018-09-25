package org.wycliffeassociates.otter.jvm.app.ui.projecthome.View

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import org.wycliffeassociates.otter.common.data.model.Project
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.projectcard
import tornadofx.*

class ProjectHomeView : View() {
    private val viewModel: ProjectHomeViewModel by inject()

    override val root = borderpane {
        center {
            datagrid<Project> {
                items = viewModel.projects
                cellCache {
                    projectcard(it) {
                        loadButton.text = messages["load"] // Replace with localized key later
                    }
                }
            }
        }
        bottom {
            hbox {
                alignment = Pos.BOTTOM_RIGHT
                padding = insets(15)
                button("", MaterialIconView(MaterialIcon.ADD))
            }
        }
    }
}
