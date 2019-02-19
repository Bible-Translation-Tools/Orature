package org.wycliffeassociates.otter.jvm.app.ui.projectgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizard
import tornadofx.*

class ProjectGridViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val collectionRepo = injector.collectionRepo

    val projects: ObservableList<Collection> = FXCollections.observableArrayList<Collection>()
    val selectedProjectProperty = SimpleObjectProperty<Collection>()

    init {
        loadProjects()
    }

    fun loadProjects() {
        collectionRepo.getRootProjects()
                .observeOnFx()
                .doOnSuccess {
                    projects.setAll(it)
                }.subscribe()
    }

    fun clearSelectedProject() {
        selectedProjectProperty.value = null
    }

    fun createProject() {
        workspace.find<ProjectWizard>().openModal()
    }

    fun deleteProject(project: Collection) {
        collectionRepo.deleteProject(project, false)
                .observeOnFx()
                .andThen(Completable.fromAction { loadProjects() })
                .subscribe()
    }

    fun selectProject(project: Collection) {
        selectedProjectProperty.value = project
    }
}
