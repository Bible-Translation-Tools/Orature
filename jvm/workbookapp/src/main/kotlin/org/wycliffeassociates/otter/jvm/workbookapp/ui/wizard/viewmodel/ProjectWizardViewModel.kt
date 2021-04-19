package org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.subjects.PublishSubject
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javax.inject.Inject
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.ProjectWizard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.view.fragments.SelectCollection
import tornadofx.SortedFilteredList
import tornadofx.ViewModel
import tornadofx.booleanBinding

class ProjectWizardViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ProjectWizardViewModel::class.java)

    @Inject lateinit var languageRepo: ILanguageRepository
    @Inject lateinit var collectionRepo: ICollectionRepository
    @Inject lateinit var resourceMetadataRepo: IResourceMetadataRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var creationUseCase: CreateProject

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
    val canGoBack: BooleanProperty = SimpleBooleanProperty(false)
    val languageConfirmed: BooleanProperty = SimpleBooleanProperty(false)

    val languageCompletedText = SimpleStringProperty()
    val resourceCompletedText = SimpleStringProperty()
    val bookCompletedText = SimpleStringProperty()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun loadLanguages() {
        loadTargetLanguages()
        loadSourceLanguages()
        loadProjects()
        selectedTargetLanguage
            .toObservable()
            .doOnError { e ->
                logger.error("Error in loading projects for selected language", e)
            }
            .subscribe { language ->
                existingProjects.setAll(projects.filter { it.resourceContainer?.language == language })
                languageCompletedText.set(language?.anglicizedName)
            }
    }

    private fun loadTargetLanguages() {
        targetLanguages.clear()
        languageRepo
            .getAll()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error initializing target languages", e)
            }
            .subscribe { retrieved ->
                targetLanguages.addAll(retrieved)
            }
    }

    private fun loadSourceLanguages() {
        sourceLanguages.clear()
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .map { collections ->
                collections.mapNotNull { collection -> collection.resourceContainer?.language }
            }
            .map { languages ->
                languages.distinct()
            }
            .doOnError { e ->
                logger.error("Error in initializing source languages", e)
            }
            .subscribe { uniqueLanguages ->
                sourceLanguages.addAll(uniqueLanguages)
            }
    }

    private fun loadProjects() {
        projects.clear()
        collectionRepo
            .getDerivedProjects()
            .doOnError { e ->
                logger.error("Error in loading projects", e)
            }
            .subscribe { retrieved ->
                projects.addAll(retrieved)
            }
    }

    fun getRootSources() {
        collectionRepo
            .getRootSources()
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in getting root resources", e)
            }
            .subscribe { retrieved ->
                collectionHierarchy.add(
                    retrieved.filter {
                        it.resourceContainer?.language == selectedSourceLanguage.value
                    }
                )
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
            .doOnError { e -> logger.error("Error in show sub collections", e) }
            .subscribe()
    }

    private fun createProject(selectedCollection: Collection) {
        selectedTargetLanguage.value?.let { language ->
            showOverlayProperty.value = true
            creationUseCase
                .create(selectedCollection, language)
                .doOnError { e ->
                    logger.error("Error in creating a project for collection: $selectedCollection", e)
                }
                .subscribe { derivedProject ->
                    val projectFilesAccessor = ProjectFilesAccessor(
                        directoryProvider,
                        selectedCollection.resourceContainer!!,
                        derivedProject.resourceContainer!!,
                        derivedProject
                    )

                    projectFilesAccessor.initializeResourceContainerInDir()
                    projectFilesAccessor.copySourceFiles()
                    projectFilesAccessor.createSelectedTakesFile()

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
        workspace.navigateBack()
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
        languageCompletedText.set(null)
        resourceCompletedText.set(null)
        loadLanguages()
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
