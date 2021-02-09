package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Maybe
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.navigation.TabGroupType
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.MyApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import javax.inject.Inject

class ProjectGridViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ProjectGridViewModel::class.java)

    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var workbookRepo: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider

    private val navigator: ChromeableStage by inject()
    private val workbookViewModel: WorkbookViewModel by inject()
    val showDeleteDialogProperty = SimpleBooleanProperty(false)

    val projects: ObservableList<Workbook> = FXCollections.observableArrayList()

    init {
        (app as MyApp).dependencyGraph.inject(this)
    }

    fun loadProjects() {
        workbookRepo.getProjects()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading projects", e)
            }
            .subscribe { _projects ->
                projects.setAll(_projects)
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
            .delete(project, true)
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
        workbook.target.resourceMetadata.let(workbookViewModel::setProjectFilesAccessor)
        navigator.navigateTo(TabGroupType.CHAPTER)
    }
}
