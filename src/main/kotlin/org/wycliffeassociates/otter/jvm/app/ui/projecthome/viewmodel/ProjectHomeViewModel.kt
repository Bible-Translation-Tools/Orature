package org.wycliffeassociates.otter.jvm.app.ui.projecthome.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.jvm.app.ui.projecteditor.view.ProjectEditor
import tornadofx.*
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.domain.collections.GetCollections
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizard

class ProjectHomeViewModel : ViewModel() {
    private val collectionRepo = Injector.collectionRepo

    val projects: ObservableList<Collection> = FXCollections.observableArrayList<Collection>()
    val selectedProjectProperty = SimpleObjectProperty<Collection>()

    init {
        loadProjects()
    }

    fun loadProjects() {
        GetCollections(collectionRepo).rootProjects()
                .observeOnFx()
                .doOnSuccess {
                    projects.setAll(it)
                }.subscribe()
    }

    fun createProject() {
        workspace.find<ProjectWizard>().openModal()
    }

    fun openProject(project: Collection) {
        selectedProjectProperty.value = project
        workspace.dock<ProjectEditor>()
    }
}
