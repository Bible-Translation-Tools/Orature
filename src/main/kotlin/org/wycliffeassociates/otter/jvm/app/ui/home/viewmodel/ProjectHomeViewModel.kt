package org.wycliffeassociates.otter.jvm.app.ui.home.ViewModel

import org.wycliffeassociates.otter.common.data.model.Project
import org.wycliffeassociates.otter.common.domain.GetProjectsUseCase
import tornadofx.*
import io.reactivex.Observable
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.jvm.app.ui.chapterPage.view.ChapterPage
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.widgets.ProjectCard

class ProjectHomeViewModel: ViewModel() {
    var projectUseCase = GetProjectsUseCase(Injector.projectDao)
    val projects : Observable<List<Project>> = projectUseCase.getProjects()
    val items = FXCollections.observableArrayList<ProjectCard>()!!
    init {
        getProjects()
    }

    private fun getProjects() {
        projectUseCase.getProjects().subscribe {
            items.setAll(
                    it.map{
                        ProjectCard(it)
                    }
            )
        }
    }
}
