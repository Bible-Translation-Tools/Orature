package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.transformation.FilteredList
import javafx.scene.control.CustomMenuItem
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.ToggleGroup
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.resourcecontainer.CoverArtAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.jvm.controls.button.SelectButton
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.enums.BookSortBy
import org.wycliffeassociates.otter.jvm.workbookapp.enums.ProjectType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import tornadofx.*
import java.io.File
import java.util.function.Predicate
import javax.inject.Inject

class BookWizardViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(BookWizardViewModel::class.java)

    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var creationUseCase: CreateProject
    @Inject lateinit var directoryProvider: IDirectoryProvider

    private val navigator: NavigationMediator by inject()

    val sourceLanguageProperty = SimpleObjectProperty<Language>()
    val targetLanguageProperty = SimpleObjectProperty<Language>()

    val projectTypeProperty = SimpleObjectProperty<ProjectType>()
    val selectedBookProperty = SimpleObjectProperty<Collection>()

    val searchQueryProperty = SimpleStringProperty("")
    val showProgressProperty = SimpleBooleanProperty(false)

    val activeProjectTitleProperty = SimpleStringProperty()
    val activeProjectCoverProperty = SimpleObjectProperty<File>()

    private val books = observableListOf<Collection>()
    private val sourceCollections = observableListOf<Collection>()
    private val selectedSourceProperty = SimpleObjectProperty<Collection>()
    val filteredBooks = FilteredList(books)
    val existingBooks = observableListOf<Collection>()
    val menuItems = observableListOf<MenuItem>()

    private var queryPredicate = Predicate<Collection> { true }

    private val sortByProperty = SimpleObjectProperty<BookSortBy>(BookSortBy.BOOK_ORDER)
    private val resourcesToggleGroup = ToggleGroup()
    private val sortByToggleGroup = ToggleGroup()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        bindFilterProperties()

        selectedSourceProperty.onChange {
            it?.let { collection ->
                loadChildren(collection)
                loadExistingProjects(collection)
            }
        }

        selectedBookProperty.onChange {
            it?.let {
                createProject(it)
            }
        }
    }

    fun loadResources() {
        collectionRepo
            .getRootSources()
            .map {
                it.filter { collection ->
                    val sourceLanguage = collection.resourceContainer?.language?.slug
                    val selectedLanguage = sourceLanguageProperty.value.slug
                    sourceLanguage == selectedLanguage
                }
            }
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading sources", e)
            }
            .subscribe { retrieved ->
                sourceCollections.addAll(retrieved)
                setFilterMenu()
            }
    }

    private fun loadChildren(parent: Collection) {
        collectionRepo
            .getChildren(parent)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in loading children", e)
            }
            .subscribe { retrieved ->
                books.setAll(retrieved)
            }
    }

    private fun loadExistingProjects(source: Collection) {
        existingBooks.clear()
        collectionRepo
            .getDerivedProjects()
            .map { list ->
                list
                    .filter {
                        it.resourceContainer?.type == ContainerType.Book
                    }
                    .filter {
                        it.resourceContainer?.language == targetLanguageProperty.value
                    }
                    .filter {
                        it.resourceContainer?.identifier == source.resourceContainer?.identifier
                    }
            }
            .doOnError { e ->
                logger.error("Error in loading existing projects", e)
            }
            .subscribe { retrieved ->
                existingBooks.addAll(retrieved)
            }
    }

    fun reset() {
        sourceCollections.clear()
        books.clear()
        existingBooks.clear()
        queryPredicate = Predicate { true }
        searchQueryProperty.set("")
        selectedBookProperty.set(null)
        selectedSourceProperty.set(null)
        projectTypeProperty.set(null)
    }

    private fun bindFilterProperties() {
        searchQueryProperty.onChange { query ->
            queryPredicate = if (query.isNullOrBlank()) {
                Predicate { true }
            } else {
                Predicate { collection ->
                    collection.slug.startsWith(query, true)
                        .or(collection.titleKey.startsWith(query, true))
                }
            }
            filteredBooks.predicate = queryPredicate
        }

        sortByProperty.onChange {
            it?.let { sortBy ->
                when (sortBy) {
                    BookSortBy.BOOK_ORDER -> books.sortBy { collection -> collection.sort }
                    BookSortBy.ALPHABETICAL -> books.sortBy { collection -> collection.titleKey }
                }
            }
        }
    }

    private fun createProject(collection: Collection) {
        targetLanguageProperty.value?.let { language ->
            showProgressProperty.set(true)

            activeProjectTitleProperty.set(collection.titleKey)
            val accessor = CoverArtAccessor(collection.resourceContainer!!, collection.slug)
            activeProjectCoverProperty.set(accessor.getArtwork())

            creationUseCase
                .create(collection, language)
                .doOnError { e ->
                    logger.error("Error in creating a project for collection: $collection", e)
                }
                .subscribe { derivedProject ->
                    val projectFilesAccessor = ProjectFilesAccessor(
                        directoryProvider,
                        collection.resourceContainer!!,
                        derivedProject.resourceContainer!!,
                        derivedProject
                    )

                    projectFilesAccessor.initializeResourceContainerInDir()
                    projectFilesAccessor.copySourceFiles()
                    projectFilesAccessor.createSelectedTakesFile()

                    showProgressProperty.set(false)
                    Platform.runLater { navigator.home() }
                }
        }
    }

    fun setFilterMenu() {
        val items = mutableListOf<MenuItem>()
        items.add(createMenuSeparator(messages["resources"]))
        items.addAll(resourcesMenuItems())
        items.add(createMenuSeparator(messages["sortBy"]))
        items.add(
            createRadioMenuItem(messages["bookOrder"], true, sortByToggleGroup) { selected ->
                if (selected) sortByProperty.set(BookSortBy.BOOK_ORDER)
            }
        )
        items.add(
            createRadioMenuItem(messages["alphabetical"], false, sortByToggleGroup) { selected ->
                if (selected) sortByProperty.set(BookSortBy.ALPHABETICAL)
            }
        )

        menuItems.setAll(items)
    }

    private fun resourcesMenuItems(): List<MenuItem> {
        return sourceCollections.mapIndexed { index, collection ->
            val preselected = index == 0
            createRadioMenuItem(collection.titleKey, preselected, resourcesToggleGroup) { selected ->
                if (selected) {
                    selectedSourceProperty.set(collection)
                }
            }
        }
    }

    private fun createMenuSeparator(label: String): MenuItem {
        return CustomMenuItem().apply {
            styleClass.add("filtered-search-bar__menu__separator")
            content = Label(label)
            isHideOnClick = false
        }
    }

    private fun createRadioMenuItem(
        label: String,
        preSelected: Boolean,
        group: ToggleGroup,
        onSelected: (Boolean) -> Unit
    ): MenuItem {
        return CustomMenuItem().apply {
            content = SelectButton().apply {
                text = label
                tooltip(label)
                selectedProperty().onChange {
                    onSelected(it)
                }
                toggleGroup = group
                isSelected = preSelected
            }
            isHideOnClick = false
        }
    }
}
