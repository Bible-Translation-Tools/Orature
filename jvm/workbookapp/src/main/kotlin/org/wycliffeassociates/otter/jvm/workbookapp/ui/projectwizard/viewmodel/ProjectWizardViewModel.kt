package org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.subjects.PublishSubject
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.ui.mainscreen.view.MainScreenView
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.view.fragments.SelectCollection
import tornadofx.*

class ProjectWizardViewModel : ViewModel() {
    private val injector: Injector by inject()
    private val languageRepo = injector.languageRepo
    private val collectionRepo = injector.collectionRepo

    val clearLanguages: PublishSubject<Boolean> = PublishSubject.create()
    val collections: ObservableList<Collection> = FXCollections.observableArrayList()
    val targetLanguages = SortedFilteredList<Language>()
    val sourceLanguages = SortedFilteredList<Language>()
    val selectedSourceLanguage = bind(true) { SimpleObjectProperty<Language>() }
    val selectedTargetLanguage = bind(true) { SimpleObjectProperty<Language>() }

    private val collectionHierarchy: ArrayList<List<Collection>> = ArrayList()

    private val projects = FXCollections.observableArrayList<Collection>()

    private val existingProjects: ObservableList<Collection> = FXCollections.observableArrayList()
    val showOverlayProperty = SimpleBooleanProperty(false)
    val creationCompletedProperty = SimpleBooleanProperty(false)

    private val creationUseCase = CreateProject(collectionRepo)

    val canGoBack: BooleanProperty = SimpleBooleanProperty(false)
    val languageConfirmed: BooleanProperty = SimpleBooleanProperty(false)

    val languageCompletedText = SimpleStringProperty()
    val resourceCompletedText = SimpleStringProperty()
    val bookCompletedText = SimpleStringProperty()

    init {
        initializeTargetLanguages()
        initializeSourceLanguages()
        loadProjects()
        selectedTargetLanguage.toObservable().subscribe { language ->
            existingProjects.setAll(projects.filter { it.resourceContainer?.language == language })
            languageCompletedText.set(language?.anglicizedName)
        }
    }

    private fun initializeTargetLanguages() {
        languageRepo
            .getAll()
            .observeOnFx()
            .subscribe { retrieved ->
                targetLanguages.addAll(retrieved)
            }
    }

    private fun initializeSourceLanguages() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .map { collections ->
                collections.mapNotNull { collection -> collection.resourceContainer?.language }
            }.map { languages ->
                languages.distinct()
            }.subscribe { uniqueLanguages ->
                sourceLanguages.addAll(uniqueLanguages)
            }
    }

    private fun loadProjects() {
        collectionRepo
            .getDerivedProjects()
            .subscribe { retrieved ->
                projects.addAll(retrieved)
            }
    }

    fun getRootSources() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .subscribe { retrieved ->
                collectionHierarchy.add(retrieved.filter {
                    it.resourceContainer?.language == selectedSourceLanguage.value
                })
                collections.setAll(collectionHierarchy.last())
            }
    }

    fun doOnUserSelection(selectedCollection: Collection) {
        if (selectedCollection.labelKey == "project") {
            createProject(selectedCollection)
        } else {
            if (selectedCollection.labelKey == "bundle") {
                resourceCompletedText.set(selectedCollection.titleKey)
            }
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
        selectedTargetLanguage.value?.let { language ->
            showOverlayProperty.value = true
            creationUseCase
                .create(selectedCollection, language)
                .subscribe { _ ->
                    find(ProjectGridViewModel::class).loadProjects()
                    showOverlayProperty.value = false
                    creationCompletedProperty.value = true
                }
        }
    }

    fun goBack() {
        when {
            collectionHierarchy.size > 1 -> {
                resourceCompletedText.set(null)
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
        loadProjects()
        languageCompletedText.set(null)
        resourceCompletedText.set(null)
    }

    fun filterLanguages(query: String?, language: Language): Boolean {
        if (query == null) return false
        return language.name.contains(query, true) ||
                language.anglicizedName.contains(query, true) ||
                language.slug.contains(query, true)
    }

    fun languagesValid(): BooleanExpression {
        return selectedSourceLanguage
            .booleanBinding(selectedTargetLanguage) {
                selectedSourceLanguage.value != null && selectedTargetLanguage.value != null
            }
    }
}