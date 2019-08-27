package org.wycliffeassociates.otter.jvm.app.ui.projectwizard.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.app.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.app.ui.projectwizard.view.fragments.SelectCollection
import tornadofx.ViewModel
import tornadofx.booleanBinding

class ProjectWizardViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val languageRepo = injector.languageRepo
    private val collectionRepo = injector.collectionRepo

    val clearLanguages: PublishSubject<Boolean> = PublishSubject.create()
    val sourceLanguageProperty = bind(true) { SimpleObjectProperty<Language>() }
    val targetLanguageProperty = bind(true) { SimpleObjectProperty<Language>() }
    val collections: ObservableList<Collection> = FXCollections.observableArrayList()
    val allLanguages: ObservableList<Language> = FXCollections.observableArrayList()
    val filteredLanguages: ObservableList<Language> = FXCollections.observableArrayList()

    private val collectionHierarchy: ArrayList<List<Collection>> = ArrayList()

    private val projects = FXCollections.observableArrayList<Collection>()

    private val existingProjects: ObservableList<Collection> = FXCollections.observableArrayList()
    val showOverlayProperty = SimpleBooleanProperty(false)
    val creationCompletedProperty = SimpleBooleanProperty(false)

    private val creationUseCase = CreateProject(collectionRepo)

    val canGoNext: BooleanProperty = SimpleBooleanProperty(false)
    val canGoBack: BooleanProperty = SimpleBooleanProperty(false)
    val languageConfirmed: BooleanProperty = SimpleBooleanProperty(false)

    init {
        languageRepo
            .getAll()
            .observeOnFx()
            .subscribe { retrieved ->
                allLanguages.setAll(retrieved)
            }

        filterSourceLanguages()
        loadProjects()

        targetLanguageProperty.toObservable().subscribe { language ->
            existingProjects.setAll(projects.filter { it.resourceContainer?.language == language })
            if (sourceLanguageProperty != null && targetLanguageProperty != null) canGoNext.set(true)
        }
    }

    private fun filterSourceLanguages() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .map { collections ->
                collections.map { collection -> collection.resourceContainer?.language }
            }.map { languages ->
                languages.distinct()
            }.subscribe { uniqueLanguages ->
                filteredLanguages.setAll(uniqueLanguages)
            }
    }

    private fun loadProjects() {
        collectionRepo
            .getRootProjects()
            .subscribe { retrieved ->
                projects.setAll(retrieved)
            }
    }

    fun getRootSources() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .subscribe { retrieved ->
                collectionHierarchy.add(retrieved.filter {
                    it.resourceContainer?.language == sourceLanguageProperty.value
                })
                collections.setAll(collectionHierarchy.last())
            }
    }

    fun doOnUserSelection(selectedCollection: Collection) {
        if (selectedCollection.labelKey == "project") {
            createProject(selectedCollection)
        } else {
            showSubcollections(selectedCollection)
        }
    }

    private fun showSubcollections(collection: Collection) {
        collectionRepo
            .getChildren(collection)
            .observeOnFx()
            .doOnSuccess { subcollections ->
                collectionHierarchy.add(subcollections)
                collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
            }
            .subscribe()
    }

    private fun createProject(selectedCollection: Collection) {
        targetLanguageProperty.value?.let { language ->
            showOverlayProperty.value = true
            creationUseCase
                .create(selectedCollection, language)
                .subscribe {
                    find(ProjectGridViewModel::class).loadProjects()
                    showOverlayProperty.value = false
                    creationCompletedProperty.value = true
                }
        }
    }

    fun goBack() {
        when {
            collectionHierarchy.size > 1 -> {
                collectionHierarchy.removeAt(collectionHierarchy.lastIndex)
                collections.setAll(collectionHierarchy.last().sortedBy { it.sort })
            }
            collectionHierarchy.size == 1 -> {
                collectionHierarchy.removeAt(0)
                find<ProjectWizard>().wizardWorkspace.navigateBack()
                canGoBack.set(false)
                languageConfirmed.set(false)
            }
            else -> {
                find<ProjectWizard>().wizardWorkspace.navigateBack()
                canGoBack.set(false)
                languageConfirmed.set(false)
            }
        }
    }

    fun goNext() {
        getRootSources()
        find<ProjectWizard>().wizardWorkspace.dock<SelectCollection>()
        languageConfirmed.set(true)
        canGoBack.set(true)
    }

    fun closeWizard() {
        canGoBack.set(false)
        languageConfirmed.set(false)
        workspace.dock<MainScreenView>()
    }

    fun doesProjectExist(project: Collection): Boolean {
        return existingProjects.map { it.titleKey }.contains(project.titleKey)
    }

    fun reset() {
        clearLanguages.onNext(true)
        collections.setAll()
        collectionHierarchy.clear()
        existingProjects.clear()
        creationCompletedProperty.value = false
        filterSourceLanguages()
        loadProjects()
    }

    fun filterLanguages(query: String): ObservableList<Language> {
        return allLanguages.filtered {
            it.name.contains(query, true) ||
                    it.anglicizedName.contains(query, true) ||
                    it.slug.contains(query, true)
        }.sorted { lang1, lang2 ->
            when {
                lang1.slug.startsWith(query, true) -> -1
                lang2.slug.startsWith(query, true) -> 1
                lang1.name.startsWith(query, true) -> -1
                lang2.name.startsWith(query, true) -> 1
                lang1.anglicizedName.startsWith(query, true) -> -1
                lang2.anglicizedName.startsWith(query, true) -> 1
                else -> 0
            }
        }
    }

    fun languagesValid() = sourceLanguageProperty.booleanBinding(targetLanguageProperty) {
        sourceLanguageProperty.value != null && targetLanguageProperty.value != null
    }
}