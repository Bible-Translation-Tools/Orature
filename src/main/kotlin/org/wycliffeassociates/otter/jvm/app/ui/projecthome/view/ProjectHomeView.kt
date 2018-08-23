package org.wycliffeassociates.otter.jvm.app.ui.projecthome.View

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.geometry.Pos
import javafx.scene.input.MouseEvent
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.widgets.ProjectCard
import tornadofx.*

class ProjectHomeView : View() {
    private val viewModel: ProjectHomeViewModel by inject()
    val projects = viewModel.projects

    override val root = borderpane {
        center {
            projects?.subscribeBy(
                    onNext = {
                        datagrid(it) {

                            cellCache {
                                ProjectCard(it).root
                            }
                        }
                    }
            )
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

    fun openProject() {
        //TODO open selected Project, need to construct view
    }
}
