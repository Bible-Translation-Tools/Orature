package org.wycliffeassociates.otter.jvm.app.ui.projecthome.ViewModel

import tornadofx.*


class ProjectHomeViewModel: ViewModel() {
    val projects = listOf("Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians",
            "2 Corinthians", "Jude", "1 John")
//    val projects : List<Project> = getAllProjects()
}