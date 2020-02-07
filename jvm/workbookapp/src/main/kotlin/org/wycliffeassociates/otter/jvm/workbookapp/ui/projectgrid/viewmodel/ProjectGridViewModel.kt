package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ProjectGridViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val collectionRepo = injector.collectionRepo
    private val workbookRepo = injector.workbookRepository
    private val directoryProvider = injector.directoryProvider

    private val navigator: ChromeableStage by inject()
    private val workbookViewModel: WorkbookViewModel by inject()

    val projects: ObservableList<Collection> = FXCollections.observableArrayList<Collection>()

    init {
        loadProjects()
    }

    fun loadProjects() {
        collectionRepo.getDerivedProjects()
            .observeOnFx()
            .subscribe { derivedProjects ->
                val bookProjects = derivedProjects.filter { it.resourceContainer?.type == ContainerType.Book }
                projects.setAll(bookProjects)
            }
    }

    fun clearSelectedProject() {
        workbookViewModel.activeWorkbookProperty.set(null)
    }

    fun createProject() {
        workspace.dock<ProjectWizard>()
    }

    fun deleteProject(project: Collection) {
        DeleteProject(collectionRepo, directoryProvider)
            .delete(project, true)
            .observeOnFx()
            .doOnComplete {
                Platform.runLater { loadProjects() }
            }
            .subscribe()
    }

    fun selectProject(targetProject: Collection) {
        collectionRepo.getSource(targetProject)
            .observeOnFx()
            .subscribe { sourceProject ->
                val workbook = workbookRepo.get(sourceProject, targetProject)
                workbookViewModel.activeWorkbookProperty.set(workbook)

                targetProject.resourceContainer?.let(workbookViewModel::setProjectAudioDirectory)

                navigator.navigateTo(TabGroupType.CHAPTER)
            }
    }
}