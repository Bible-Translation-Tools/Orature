package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
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
        collectionRepo.getRootProjects()
            .observeOnFx()
            .doOnSuccess {
                projects.setAll(it)
            }.subscribe()
    }

    fun clearSelectedProject() {
        workbookViewModel.activeWorkbookProperty.set(null)
    }

    fun createProject() {
        workspace.dock<ProjectWizard>()
    }

    fun deleteProject(project: Collection) {
        collectionRepo.deleteProject(project, false)
            .observeOnFx()
            .andThen(Completable.fromAction { loadProjects() })
            .subscribe()
    }

    fun selectProject(targetProject: Collection) {
        collectionRepo.getSource(targetProject)
            .observeOnFx()
            .subscribe { sourceProject ->
                val workbook = workbookRepo.get(sourceProject, targetProject)
                workbookViewModel.activeWorkbookProperty.set(workbook)

                setProjectAudioDirectory(targetProject, sourceProject)
                navigator.navigateTo(TabGroupType.CHAPTER)
            }
    }

    private fun setProjectAudioDirectory(targetProject: Collection, sourceProject: Collection) {
        val projectAudioDir = directoryProvider.getProjectAudioDirectory(
            sourceMetadata = sourceProject.resourceContainer ?: throw RuntimeException("No source metadata found."),
            book = targetProject
        )
        workbookViewModel.activeProjectAudioDirectoryProperty.set(projectAudioDir)
    }
}