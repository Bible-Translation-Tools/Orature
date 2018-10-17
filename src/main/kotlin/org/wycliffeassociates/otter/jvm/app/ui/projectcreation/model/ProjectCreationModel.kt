package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.binding.BooleanExpression
import javafx.beans.property.SimpleBooleanProperty
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
            Injector.collectionRepo, Injector.projectRepo
    )
    var sourceLanguageProperty: Language by property()
    var targetLanguageProperty: Language by property()
    var anthologyList: ObservableList<Collection> by property(FXCollections.observableArrayList())
    var bookList: ObservableList<Collection> by property(FXCollections.observableArrayList())
    var collectionList: ObservableList<Collection> = FXCollections.observableArrayList()

    val languages: ObservableList<Language> = FXCollections.observableArrayList()
    val resources: ObservableList<Collection> = FXCollections.observableArrayList()

    val bookLevelReached = SimpleBooleanProperty(false)

//    val collectionArray : Array<ObservableList<Collection>> =

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
                    collectionList.setAll(retrieved.filter {
                        it.resourceContainer.language == sourceLanguageProperty
                    })
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


    fun getResourceChildren(parentCollection: Collection) {
        creationUseCase.getResourceChildren(parentCollection)
                .observeOnFx()
                .doOnSuccess {
                    collectionList.setAll(it)
                }
                .subscribe()
    }

    fun createProject(selectedCollection: Collection) {
        creationUseCase
                .newProject(
                        Collection(
                                selectedCollection.sort,
                                selectedCollection.slug,
                                "project",
                                selectedCollection.titleKey,
                                selectedCollection.resourceContainer
                        )
                )
                .flatMapCompletable {
                    creationUseCase.updateSource(it, selectedCollection)
                }
                .subscribe()
    }

}