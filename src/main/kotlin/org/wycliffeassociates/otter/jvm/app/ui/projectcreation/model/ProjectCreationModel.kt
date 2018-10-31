package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ProjectCollection
import org.wycliffeassociates.otter.common.domain.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeViewModel
import tornadofx.*

class ProjectCreationModel {
    private val creationUseCase = CreateProject(
            Injector.languageRepo,
            Injector.sourceRepo,
            Injector.collectionRepo,
            Injector.projectRepo,
            Injector.chunkRepository,
            Injector.metadataRepo,
            Injector.directoryProvider
    )
    var sourceLanguage: Language? by property()
    var targetLanguage: Language? by property()
    var targetLanguageProperty = getProperty(ProjectCreationModel::targetLanguage)
    var collectionList: ObservableList<Collection> = FXCollections.observableArrayList()
    val languages: ObservableList<Language> = FXCollections.observableArrayList()
    private var collectionStore: ArrayList<List<Collection>> = ArrayList()
    private var allProjects = find(ProjectHomeViewModel::class).allProjects
    var selectedLanguageProjects: List<ProjectCollection> by property()
    var selectedLanguageProjectsProperty = getProperty(ProjectCreationModel::selectedLanguageProjects)

    init {
        creationUseCase
                .getAllLanguages()
                .observeOnFx()
                .subscribe { retrieved ->
                    languages.setAll(retrieved)
                }

        targetLanguageProperty.onChange {
            selectedLanguageProjects = allProjects.filter {
                it.resourceContainer?.language == targetLanguage
            }
        }
    }

    fun getRootSources() {
        creationUseCase
                .getSourceRepos()
                .observeOnFx()
                .subscribe { retrieved ->
                    collectionStore.add(retrieved.filter {
                        it.resourceContainer?.language == sourceLanguage
                    })
                    collectionList.setAll(collectionStore.last())
                }
    }

    fun doOnUserSelection(selectedCollection: Collection, workspace: Workspace) {
        if (selectedCollection.labelKey == "book") {
            createProject(selectedCollection)
            workspace.dock<ProjectHomeView>()
        } else {
            showCollectionChildren(selectedCollection)
        }
    }


    private fun showCollectionChildren(parentCollection: Collection) {
        creationUseCase
                .getResourceChildren(parentCollection)
                .observeOnFx()
                .doOnSuccess {
                    collectionStore.add(it)
                    collectionList.setAll(collectionStore.last().sortedBy { it.sort })
                }
                .subscribe()
    }

    fun goBack(projectWizard: Wizard) {
        when {
            collectionStore.size > 1 -> {
                collectionStore.removeAt(collectionStore.size - 1)
                collectionList.setAll(collectionStore.last().sortedBy { it.sort })
            }

            collectionStore.size == 1 -> {
                collectionStore.removeAt(0)
                projectWizard.back()
            }
            else -> projectWizard.back()
        }
    }

    private fun createProject(selectedCollection: Collection) {
        if(targetLanguage != null) {
            creationUseCase
                    .newProject(selectedCollection, targetLanguage!!)
                    .subscribe()
        }
    }

    fun reset() {
        sourceLanguage = null
        targetLanguage = null
        collectionList.setAll()
        collectionStore = ArrayList()
        selectedLanguageProjectsProperty.value = listOf()
    }

}