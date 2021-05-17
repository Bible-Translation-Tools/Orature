package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.ProjectWizard
import tornadofx.*
import javax.inject.Inject
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.WorkbookPage
import java.io.File
import javax.inject.Provider

class ProjectGridViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ProjectGridViewModel::class.java)

    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var workbookRepo: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var deleteProjectProvider: Provider<DeleteProject>

    private val workbookDataStore: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()
    val showDeleteDialogProperty = SimpleBooleanProperty(false)
    val activeProjectTitleProperty = SimpleStringProperty()
    val activeProjectCoverProperty = SimpleObjectProperty<File>()

    val projects: ObservableList<Workbook> = FXCollections.observableArrayList()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
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
        workbookDataStore.activeWorkbookProperty.set(null)
    }

    fun createProject() {
        navigator.dock<ProjectWizard>()
    }

    fun deleteWorkbook(workbook: Workbook) {
        showDeleteDialogProperty.set(true)
        activeProjectTitleProperty.set(workbook.target.title)
        activeProjectCoverProperty.set(workbook.coverArtAccessor.getArtwork())

        val deleteProject = deleteProjectProvider.get()

        workbookRepo.closeWorkbook(workbook)
        deleteProject
            .delete(workbook, true)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in deleting project: ${workbook.target.slug} ${workbook.target.language.slug}", e)
            }
            .doFinally {
                activeProjectTitleProperty.set(null)
                activeProjectCoverProperty.set(null)
            }
            .subscribe {
                showDeleteDialogProperty.set(false)
                Platform.runLater { loadProjects() }
            }
    }

    fun selectProject(workbook: Workbook) {
        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbook.target.resourceMetadata.let(workbookDataStore::setProjectFilesAccessor)
        navigator.dock<WorkbookPage>()
    }
}
