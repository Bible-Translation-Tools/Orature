package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Maybe
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ProjectGridViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ProjectGridViewModel::class.java)

    private val injector: Injector by inject()
    private val collectionRepo = injector.collectionRepo
    private val workbookRepo = injector.workbookRepository
    private val directoryProvider = injector.directoryProvider

    private val navigator: ChromeableStage by inject()
    private val workbookViewModel: WorkbookViewModel by inject()
    val showDeleteDialogProperty = SimpleBooleanProperty(false)

    val projects: ObservableList<Workbook> = FXCollections.observableArrayList()

    fun loadProjects() {
        collectionRepo.getDerivedProjects()
            .toObservable()
            .observeOnFx()
            .map { derivedProjects ->
                derivedProjects.filter { it.resourceContainer?.type == ContainerType.Book }
            }
            .flatMapIterable { it }
            .map {
                getWorkbook(it)
            }
            .collectInto(mutableListOf<Maybe<Workbook>>(), { list, item -> list.add(item) })
            .doOnError { e ->
                logger.error("Error in loading projects", e)
            }
            .subscribe { derivedProjects ->
                val bookProjects = derivedProjects.mapNotNull { it.blockingGet() }
                projects.setAll(bookProjects)
            }
    }

    fun clearSelectedProject() {
        workbookViewModel.activeWorkbookProperty.set(null)
    }

    fun createProject() {
        workspace.dock<ProjectWizard>()
    }

    fun deleteProject(project: Workbook) {
        showDeleteDialogProperty.set(true)
        workbookRepo.closeWorkbook(project)
        DeleteProject(collectionRepo, directoryProvider)
            .delete(project.target.toCollection(), true)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in deleting project: ${project.target.slug} ${project.target.language.slug}", e)
            }
            .subscribe {
                showDeleteDialogProperty.set(false)
                Platform.runLater { loadProjects() }
            }
    }

    fun selectProject(workbook: Workbook) {
        workbookViewModel.activeWorkbookProperty.set(workbook)
        workbook.target.resourceMetadata.let(workbookViewModel::setProjectAudioDirectory)
        navigator.navigateTo(TabGroupType.CHAPTER)
    }

    private fun getWorkbook(targetProject: Collection): Maybe<Workbook> {
        return collectionRepo.getSource(targetProject)
            .map { sourceProject ->
                workbookRepo.get(sourceProject, targetProject)
            }
    }
}
