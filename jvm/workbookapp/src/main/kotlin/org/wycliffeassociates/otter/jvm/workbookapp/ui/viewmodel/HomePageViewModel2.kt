package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.domain.collections.UpdateProject
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDescriptorRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.controls.model.ProjectGroupKey
import org.wycliffeassociates.otter.jvm.controls.model.ProjectGroupCardModel
import org.wycliffeassociates.otter.jvm.utils.ListenerDisposer
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.WorkbookPage
import tornadofx.*
import java.time.LocalDateTime
import java.util.function.Predicate
import javax.inject.Inject

class HomePageViewModel2 : ViewModel() {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var workbookRepo: IWorkbookRepository

    @Inject
    lateinit var workbookDescriptorRepo: IWorkbookDescriptorRepository

    @Inject
    lateinit var updateProjectUseCase: UpdateProject

    @Inject
    lateinit var deleteProjectUseCase: DeleteProject

    private val workbookDS: WorkbookDataStore by inject()
    private val navigator: NavigationMediator by inject()
    private val workbookDataStore: WorkbookDataStore by inject()

    val projectGroups = observableListOf<ProjectGroupCardModel>()
    val bookList = observableListOf<WorkbookDescriptor>()
    private val filteredBooks = FilteredList<WorkbookDescriptor>(bookList)
    private val disposableListeners = mutableListOf<ListenerDisposer>()

    val sortedBooks = SortedList<WorkbookDescriptor>(filteredBooks)
    val selectedProjectGroup = SimpleObjectProperty<ProjectGroupKey>()
    val bookSearchQueryProperty = SimpleStringProperty("")
    val isLoadingProperty = SimpleBooleanProperty(false)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun dock() {
        clearProjects()
        setupBookSearchListener()
        loadProjects()
    }

    fun undock() {
        projectGroups.clear()
        bookList.clear()
        disposableListeners.forEach { it.dispose() }
        disposableListeners.clear()
    }

    /**
     * Closes all open projects, closing their connections in the workbook repository.
     */
    private fun clearProjects() {
        logger.info("Closing open workbooks")
        workbookDataStore.activeWorkbookProperty.value?.let {
            workbookRepo.closeWorkbook(it)
        }
        workbookDataStore.activeWorkbookProperty.set(null)
        projectGroups.clear()
        bookList.clear()
    }

    private fun setupBookSearchListener() {
        bookSearchQueryProperty.onChangeWithDisposer { q ->
            val query = q?.trim() ?: ""
            filteredBooks.predicate = if (query.isEmpty()) {
                Predicate { true }
            } else {
                Predicate { book ->
                    book.slug.contains(query, true)
                        .or(book.title.contains(query, true))
                }
            }
        }.apply { disposableListeners.add(this) }
    }

    fun loadProjects(onFinishCallback: () -> Unit = {}) {
        isLoadingProperty.set(true)
        // reset sort to default book order
        sortedBooks.comparator = Comparator { wb1, wb2 ->
            wb1.sort.compareTo(wb2.sort)
        }
        workbookDescriptorRepo.getAll()
            .observeOnFx()
            .subscribe { books ->
                updateBookList(books)
                runLater {
                    onFinishCallback()
                    isLoadingProperty.set(false)
                }
            }
    }

    fun selectBook(workbookDescriptor: WorkbookDescriptor) {
        val projectGroup = selectedProjectGroup.value
        workbookDS.currentModeProperty.set(selectedProjectGroup.value.mode)

        val projects = workbookRepo.getProjects().blockingGet()
        val existingProject = projects.firstOrNull { existingProject ->
            existingProject.source.language.slug == projectGroup.sourceLanguage &&
                    existingProject.target.language.slug == projectGroup.targetLanguage &&
                    existingProject.target.slug == workbookDescriptor.slug
        }

        existingProject?.let { workbook ->
            openWorkbook(workbook)
            navigator.dock<WorkbookPage>()
        }
    }

    fun deleteProjectGroup(books: List<WorkbookDescriptor>) {
        if (books.all { it.progress == 0.0 }) {
            logger.info("Deleting project group: ${selectedProjectGroup.value.sourceLanguage} -> ${selectedProjectGroup.value.targetLanguage}")

            deleteProjectUseCase.deleteProjects(books)
                .observeOnFx()
                .subscribe {
                    loadProjects()
                }
        }
    }

    fun deleteBook(workbookDescriptor: WorkbookDescriptor): Completable {
        logger.info("Deleting book: ${workbookDescriptor.slug}")

        return deleteProjectUseCase.delete(workbookDescriptor)
            .observeOnFx()
    }

    fun openInFilesManager(path: String) = directoryProvider.openInFileManager(path)

    private fun updateBookList(books: List<WorkbookDescriptor>) {
        if (books.isEmpty()) {
            bookList.clear()
            projectGroups.clear()
            return
        }

        val projectGroups = books.groupBy {
            ProjectGroupKey(it.sourceLanguage.slug, it.targetLanguage.slug, it.mode)
        }
        projectGroups
            .map {
                val book = it.value.first()
                val mostRecentBook = it.value.maxByOrNull { it.lastModified?.nano ?: -1 }
                ProjectGroupCardModel(
                    book.sourceLanguage,
                    book.targetLanguage,
                    book.mode,
                    mostRecentBook?.lastModified,
                    it.value.toObservable()
                )
            }
            .sortedByDescending { it.modifiedTs }
            .let { modelList ->
                this.projectGroups.setAll(modelList)
                modelList.firstOrNull()?.let { cardModel ->
                    selectedProjectGroup.set(cardModel.getKey())
                    bookList.setAll(cardModel.books)
                }
            }
    }

    private fun openWorkbook(workbook: Workbook) {
        workbookDS.activeWorkbookProperty.set(workbook)
        initializeProjectFiles(workbook)
        updateWorkbookModifiedDate(workbook)
    }

    private fun initializeProjectFiles(workbook: Workbook) {
        val linkedResource = workbook
            .source
            .linkedResources
            .firstOrNull { it.identifier == workbook.source.resourceMetadata.identifier }

        workbook.projectFilesAccessor.initializeResourceContainerInDir(false)
        workbook.projectFilesAccessor.copySourceFiles(linkedResource)
        workbook.projectFilesAccessor.createSelectedTakesFile()
        workbook.projectFilesAccessor.createChunksFile()
        workbook.projectFilesAccessor.setProjectMode(workbookDS.currentModeProperty.value)
    }

    private fun updateWorkbookModifiedDate(workbook: Workbook) {
        val project = workbook.target.toCollection()
        project.modifiedTs = LocalDateTime.now()
        updateProjectUseCase.update(project).subscribe()
    }
}