package org.wycliffeassociates.otter.jvm.app.ui.projecthome.View

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Pos
import org.wycliffeassociates.otter.common.ui.widgets.IProjectCard
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.ProjectCard
import tornadofx.*

class ProjectHomeView : View(), IProjectCard {
    private val viewModel: ProjectHomeViewModel by inject()
    override val root = borderpane {
        center {
            datagrid(viewModel.projects) {

                cellCache {
                    ProjectCard(it, openProject = ::openProject).root
                }
            }
        }
        bottom {
            hbox {
                alignment = Pos.BOTTOM_RIGHT
                padding = insets(10)
                this += button("", MaterialIconView(MaterialIcon.ADD)) {
                }
            }
        }
    }

    override fun openProject() {
        //TODO("waiting on next view") //To change body of created functions use File | Settings | File Templates.
//        workspace.dock<Tothenextscreen>()
    }
}
