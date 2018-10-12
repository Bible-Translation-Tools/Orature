package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.collections.FXCollections
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.common.domain.GetProjects
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.ProjectCreationWizard
import tornadofx.*

class ProjectHomeModel {
    val projectUseCase = GetProjects(Injector.projectRepo)

    val allProjects = FXCollections.observableArrayList<Collection>()

    fun getAllProjects() {
        projectUseCase.getAllRoot()
                .observeOnFx()
                .doOnSuccess {
                    allProjects.setAll(it)
                }.subscribe()
    }

    fun createProject(workspace: Workspace) {
        workspace.dockInNewScope<ProjectCreationWizard>()
    }
}