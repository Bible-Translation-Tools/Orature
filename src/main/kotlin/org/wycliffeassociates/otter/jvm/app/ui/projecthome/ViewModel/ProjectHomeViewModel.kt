package org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel

import org.wycliffeassociates.otter.common.data.dao.Dao
import org.wycliffeassociates.otter.common.data.model.Project
import org.wycliffeassociates.otter.common.domain.GetProjectsUseCase
import tornadofx.*
import org.wycliffeassociates.otter.common.data.model.Language
import io.reactivex.Observable
import java.util.*

class ProjectHomeViewModel: ViewModel() {
    // TODO(need repo)?
    val getProjectsUseCase = GetProjectsUseCase()
    val projects : List<Project> = getProjectsUseCase.getProjects().toList().blockingGet().flatMap{it}
}
