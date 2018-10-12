package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.view.ProjectCreationWizard
import tornadofx.*

class ProjectHomeViewModel : ViewModel() {
    val model = ProjectHomeModel()
    val allProjects = model.allProjects

    fun getAllProjects() = model.getAllProjects()
    fun createProject() = model.createProject(this.workspace)
}
