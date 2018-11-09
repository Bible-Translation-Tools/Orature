package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import javafx.beans.property.SimpleObjectProperty
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view.ProjectEditor
import tornadofx.*
import org.wycliffeassociates.otter.common.data.model.Collection

class ProjectHomeViewModel : ViewModel() {
    val model = ProjectHomeModel()
    val allProjects = model.allProjects
    val selectedProjectProperty = SimpleObjectProperty<Collection>()

    fun getAllProjects() = model.getAllProjects()
    fun createProject() = model.createProject(this.workspace)

    fun openProject(project: Collection) {
        selectedProjectProperty.set(project)
        workspace.dock<ProjectEditor>()
    }
}
