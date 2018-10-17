package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.projecthome.ProjectHomeView
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
    var sourceLanguageProperty: Language by property()
    var targetLanguageProperty: Language by property()
    var collectionList: ObservableList<Collection> = FXCollections.observableArrayList()
    val languages: ObservableList<Language> = FXCollections.observableArrayList()
    var collectionStore: ArrayList<List<Collection>> = ArrayList()
    //var creationDepthProperty = SimpleIntegerProperty(0)
    var creationDepth: Int = 0
    var bookDepthReached = false

    init {
        creationUseCase.getAllLanguages()
                .observeOnFx()
                .subscribe { retrieved ->
                    languages.setAll(retrieved)
                }
    }

    fun getSourceRepos() {
        creationUseCase.getSourceRepos()
                .observeOnFx()
                .subscribe { retrieved ->
                    collectionStore.add(retrieved.filter {
                        it.resourceContainer?.language == sourceLanguageProperty
                    })
                    collectionList.setAll(collectionStore[collectionStore.size - 1])
                }
    }

    fun checkLevel(selectedCollection: Collection, workspace: Workspace) {
        if (collectionList[0].labelKey == "book") {
            createProject(selectedCollection)
            workspace.dock<ProjectHomeView>()
        } else {
            getResourceChildren(selectedCollection)
        }
    }


    private fun getResourceChildren(parentCollection: Collection) {
        creationUseCase.getResourceChildren(parentCollection)
                .observeOnFx()
                .doOnSuccess {
                    collectionStore.add(it)
                    collectionList.setAll(collectionStore[collectionStore.size - 1])
                }
                .subscribe()
    }

    fun getPreviousCollections(projectWizard: Wizard) {
        if (collectionStore.size > 1) {
            collectionStore.removeAt(collectionStore.size - 1)
            collectionList.setAll(collectionStore[collectionStore.size - 1])
        } else {
            projectWizard.back()
        }
    }

    fun getDepth(selectedCollection: Collection) {
        if (creationDepth.equals(0)) {
            while (!bookDepthReached) {
                creationUseCase.getResourceChildren(selectedCollection)
                        .observeOn(JavaFxScheduler.platform())
                        .subscribe { retrieved ->
                            if (retrieved.get(0).labelKey == "book") {
                                bookDepthReached = true
                            } else {
                                creationDepth++
                            }

                        }
            }
        }
    }

    private fun createProject(selectedCollection: Collection) {
        creationUseCase
                .newProject(selectedCollection, targetLanguageProperty)
                .subscribe()
    }

}