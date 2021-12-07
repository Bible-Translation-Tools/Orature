/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
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
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.collections.UpdateProject
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.Artwork
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.controls.button.SelectButton
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.enums.BookSortBy
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.BookCardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.TranslationCardModel
import tornadofx.*
import java.io.File
import java.util.function.Predicate
import javax.inject.Inject

class BookWizardViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(BookWizardViewModel::class.java)

    @Inject
    lateinit var collectionRepo: ICollectionRepository

    @Inject
    lateinit var creationUseCase: CreateProject

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var workbookRepo: IWorkbookRepository

    @Inject
    lateinit var updateProjectUseCase: UpdateProject

    private val navigator: NavigationMediator by inject()

    val translationProperty = SimpleObjectProperty<TranslationCardModel>()
    val selectedBookProperty = SimpleObjectProperty<Collection>()

    val searchQueryProperty = SimpleStringProperty("")
    val showProgressProperty = SimpleBooleanProperty(false)

    val activeProjectTitleProperty = SimpleStringProperty()
    val activeProjectCoverProperty = SimpleObjectProperty<File>()

    private val books = observableListOf<BookCardData>()
    val sourceCollections = observableListOf<Collection>()
    val selectedSourceProperty = SimpleObjectProperty<Collection>()
    val filteredBooks = FilteredList(books)
    val existingBooks = observableListOf<Workbook>()
    val menuItems = observableListOf<MenuItem>()

    private var queryPredicate = Predicate<BookCardData> { true }

    private val sortByProperty = SimpleObjectProperty<BookSortBy>(BookSortBy.BOOK_ORDER)
    private val resourcesToggleGroup = ToggleGroup()
    private val sortByToggleGroup = ToggleGroup()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)

        bindFilterProperties()

        selectedSourceProperty.onChange {
            it?.let { collection ->
                loadChildren(collection)
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
                    val selectedLanguage = translationProperty.value.sourceLanguage.slug
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
                val bookViewDataList = retrieved
                    .map { collection ->
                        val artwork = retrieveArtworkAsync(collection)
                        BookCardData(collection, artwork)
                    }
                books.setAll(bookViewDataList)
            }
    }

    fun loadExistingProjects() {
        val translation = Translation(
            translationProperty.value.sourceLanguage,
            translationProperty.value.targetLanguage,
            translationProperty.value.modifiedTs
        )
        workbookRepo
            .getProjects(translation)
            .doOnError { e ->
                logger.error("Error in loading existing projects", e)
            }
            .subscribe { retrieved ->
                existingBooks.setAll(retrieved)
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
    }

    private fun bindFilterProperties() {
        searchQueryProperty.onChange { query ->
            queryPredicate = if (query.isNullOrBlank()) {
                Predicate { true }
            } else {
                Predicate { viewData ->
                    viewData.collection.slug.contains(query, true)
                        .or(viewData.collection.titleKey.contains(query, true))
                }
            }
            filteredBooks.predicate = queryPredicate
        }

        sortByProperty.onChange {
            it?.let { sortBy ->
                when (sortBy) {
                    BookSortBy.BOOK_ORDER -> books.sortBy { it.collection.sort }
                    BookSortBy.ALPHABETICAL -> books.sortBy { it.collection.titleKey }
                }
            }
        }
    }

    private fun createProject(collection: Collection) {
        translationProperty.value?.let { translation ->
            showProgressProperty.set(true)

            val artworkAccessor = ArtworkAccessor(
                directoryProvider,
                collection.resourceContainer!!,
                collection.slug
            )
            activeProjectTitleProperty.set(collection.titleKey)
            activeProjectCoverProperty.set(
                artworkAccessor.getArtwork(ImageRatio.FOUR_BY_ONE)?.file
            )

            creationUseCase
                .create(collection, translation.targetLanguage)
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

    private fun retrieveArtworkAsync(project: Collection): Observable<Artwork> {
        val artwork = ReplaySubject.create<Artwork>(1)
        Completable
            .fromAction {
                if (project.resourceContainer != null) {
                    ArtworkAccessor(
                        directoryProvider,
                        project.resourceContainer!!,
                        project.slug
                    ).getArtwork(ImageRatio.TWO_BY_ONE)?.let { art ->
                        artwork.onNext(art)
                    }
                }
            }
            .doOnError {
                logger.error("Error while retrieving artwork for project: ${project.slug}", it)
            }
            .doFinally { artwork.onComplete() }
            .subscribeOn(Schedulers.io())
            .subscribe()

        return artwork
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
