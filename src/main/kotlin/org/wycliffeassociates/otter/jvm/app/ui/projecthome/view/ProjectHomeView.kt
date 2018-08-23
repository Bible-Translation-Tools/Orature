package org.wycliffeassociates.otter.jvm.app.ui.projecthome.View

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import io.reactivex.rxkotlin.subscribeBy
import javafx.geometry.Pos
import org.wycliffeassociates.otter.common.domain.GetProjectsUseCase
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel.ProjectHomeViewModel
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.widgets.ProjectCard
import tornadofx.*

class ProjectHomeView : View() {
    private val viewModel: ProjectHomeViewModel by inject()
    val getProjectsUseCase = GetProjectsUseCase(Injector.projectDao)
    val projects = viewModel.projects

    init {
        viewModel.projectUseCase = getProjectsUseCase
    }

    override val root = borderpane {
        center {
            projects?.subscribeBy(
                    onNext = {
                        datagrid(it) {

                            cellCache {
                                ProjectCard(it, openProject = ::openProject)
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
