package org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel

import tornadofx.*
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeModel

class ProjectHomeViewModel: ViewModel() {
    private val model = ProjectHomeModel()
    val projects = model.projects
}
