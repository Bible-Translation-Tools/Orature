package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.disposables.Disposable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.collections.transformation.SortedList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
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
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.*
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.function.Predicate
import javax.inject.Inject

const val NOTIFICATION_DURATION_SEC = 5.0

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
    val selectedProjectGroupProperty = SimpleObjectProperty<ProjectGroupKey>()
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
        val projectGroup = selectedProjectGroupProperty.value
        workbookDS.currentModeProperty.set(selectedProjectGroupProperty.value.mode)

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

    /**
     * Temporarily removes the project group from the view as if the project was "deleted".
     * This is used in conjunction with time-out enabled deletion which allows the user to
     * cancel/undo the action.
     */
    fun removeProjectFromList(cardModel: ProjectGroupCardModel) {
        projectGroups.remove(cardModel)
        selectedProjectGroupProperty.set(projectGroups.firstOrNull()?.getKey())
        bookList.setAll(projectGroups.firstOrNull()?.books ?: listOf())
    }

    /**
     * Deletes the project group after a specified timeout. During this time,
     * the user can choose to cancel (undo) the action by calling dispose()
     * from the disposable.
     *
     * @return the disposable (cancellable) subscription to the deletion task.
     */
    fun deleteProjectGroupWithTimer(cardModel: ProjectGroupCardModel): Disposable {
        val timeoutMillis = NOTIFICATION_DURATION_SEC * 1000

        val completable: Completable = Completable.create { emitter ->
            val timerDisposable = Completable
                .timer(timeoutMillis.toLong(), TimeUnit.MILLISECONDS)
                .andThen(deleteProjectUseCase.deleteProjects(cardModel.books))
                .observeOnFx()
                .doOnComplete {
                    logger.info("Deleted project group: ${cardModel.sourceLanguage.name} -> ${cardModel.targetLanguage.name}.")
                    emitter.onComplete()
                }
                .subscribe()

            emitter.setDisposable(timerDisposable)
        }

        return completable.subscribe()
    }

    fun deleteBook(workbookDescriptor: WorkbookDescriptor): Completable {
        logger.info("Deleting book: ${workbookDescriptor.slug}")

        return deleteProjectUseCase.delete(workbookDescriptor)
            .observeOnFx()
    }

    fun openInFilesManager(path: String) = directoryProvider.openInFileManager(path)

    fun loadContributors(workbookDescriptor: WorkbookDescriptor): Single<List<Contributor>> {
        return Single
            .fromCallable {
                loadContributorsFromDerivedMetadata(
                    sourceMetadata = workbookDescriptor.sourceCollection.resourceContainer!!,
                    targetMetadata = workbookDescriptor.targetCollection.resourceContainer!!
                )
            }
            .doOnError { logger.error("Error while loading contributor info.", it) }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
    }

    private fun loadContributorsFromDerivedMetadata(
        sourceMetadata: ResourceMetadata,
        targetMetadata: ResourceMetadata
    ): List<Contributor> {
        val der = directoryProvider.getDerivedContainerDirectory(targetMetadata, sourceMetadata)
        return ResourceContainer.load(der).use { rc ->
            rc.manifest.dublinCore.contributor.map { Contributor(it) }
        }
    }

    fun saveContributors(contributors: List<Contributor>, workbookDescriptor: WorkbookDescriptor) {
        Completable
            .fromAction {
                val der = directoryProvider.getDerivedContainerDirectory(
                    workbookDescriptor.targetCollection.resourceContainer!!,
                    workbookDescriptor.sourceCollection.resourceContainer!!
                )
                ResourceContainer.load(der).use { rc ->
                    rc.manifest.dublinCore.contributor = contributors.map { it.name }.toMutableList()
                    rc.writeManifest()
                }
                bookList.forEach {
                    val workbook = workbookRepo.get(it.sourceCollection, it.targetCollection)
                    if (workbook.projectFilesAccessor.isInitialized()) {
                        workbook.projectFilesAccessor.setContributorInfo(contributors)
                    }
                }
            }
            .doOnError { logger.error("Error while saving contributor info.", it) }
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .subscribe()
    }

    fun mergeContributorFromImport(workbookDescriptor: WorkbookDescriptor) {
        val workbook = workbookRepo.get(workbookDescriptor.sourceCollection, workbookDescriptor.targetCollection)
        if (workbook.projectFilesAccessor.isInitialized()) {
            val set = loadContributorsFromDerivedMetadata(
                sourceMetadata = workbook.source.resourceMetadata,
                targetMetadata = workbook.target.resourceMetadata
            ).toMutableSet()
            val contributors = workbook.projectFilesAccessor.getContributorInfo()
            set.addAll(contributors)
            saveContributors(set.toList(), workbookDescriptor)
        }
    }

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
            .map { entry ->
                val bookList = entry.value
                val book = bookList.first()
                val mostRecentBook = bookList
                    .filter { it.lastModified != null }
                    .maxByOrNull { it.lastModified!! }

                ProjectGroupCardModel(
                    book.sourceLanguage,
                    book.targetLanguage,
                    book.mode,
                    mostRecentBook?.lastModified,
                    bookList.toObservable()
                )
            }
            .sortedByDescending { it.modifiedTs }
            .let { modelList ->
                this.projectGroups.setAll(modelList)
                modelList.firstOrNull()?.let { cardModel ->
                    selectedProjectGroupProperty.set(cardModel.getKey())
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

        val contributors = loadContributorsFromDerivedMetadata(
            sourceMetadata = workbook.source.resourceMetadata,
            targetMetadata = workbook.target.resourceMetadata
        )

        workbook.projectFilesAccessor.initializeResourceContainerInDir(false)
        workbook.projectFilesAccessor.copySourceFiles(linkedResource)
        workbook.projectFilesAccessor.createSelectedTakesFile()
        workbook.projectFilesAccessor.createChunksFile()
        workbook.projectFilesAccessor.setContributorInfo(contributors)
        workbook.projectFilesAccessor.setProjectMode(workbookDS.currentModeProperty.value)
    }

    private fun updateWorkbookModifiedDate(workbook: Workbook) {
        val project = workbook.target.toCollection()
        project.modifiedTs = LocalDateTime.now()
        updateProjectUseCase.update(project).subscribe()
    }
}