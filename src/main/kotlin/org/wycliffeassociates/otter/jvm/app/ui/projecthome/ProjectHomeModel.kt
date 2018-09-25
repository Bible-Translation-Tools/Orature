package org.wycliffeassociates.otter.jvm.app.ui.projecthome

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Book
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.Project

class ProjectHomeModel {
    // This data will eventually come from the GetProjects use case
    val projects: ObservableList<Project> = FXCollections.observableList(listOf(
            Project(
                    0,
                    Language(0, "fr", "Français", false, "French"),
                    Language(0, "en", "English", true, "English"),
                    Book(0, "Genesis", 1)
            ),
            Project(
                    0,
                    Language(0, "ar", "العَرَبِيَّة", false, "Arabic"),
                    Language(0, "en", "English", true, "English"),
                    Book(0, "Romans", 1)
            )
    ))
}