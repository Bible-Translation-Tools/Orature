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
import org.wycliffeassociates.otter.jvm.workbookapp.ui.chromeablestage.ChromeableStage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ProjectGridViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ProjectGridViewModel::class.java)

    private val collectionRepo: ICollectionRepository by di()
    private val workbookRepo: IWorkbookRepository by di()
    private val directoryProvider: IDirectoryProvider by di()

    private val navigator: ChromeableStage by inject()
    private val workbookViewModel: WorkbookViewModel by inject()
    val showDeleteDialogProperty = SimpleBooleanProperty(false)

    val projects: ObservableList<Workbook> = FXCollections.observableArrayList()

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
