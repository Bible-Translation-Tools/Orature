package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.CreateProject
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*

class ProjectCreationModel {
    private val creationUseCase = CreateProject(
            Injector.languageRepo,
            Injector.sourceRepo,
            Injector.collectionRepo, Injector.projectRepo
    )
    var sourceLanguageProperty: Language by property()
    var targetLanguageProperty: Language by property()
    var selectedResource: Collection by property()
    var selectedAnthology: Collection by property()
    var selectedBook: Collection by property()
    var anthologyList: ObservableList<Collection> by property(FXCollections.observableArrayList())
    var bookList: ObservableList<Collection> by property(FXCollections.observableArrayList())

    val languages: ObservableList<Language> = FXCollections.observableArrayList()
    val resources: ObservableList<Collection> = FXCollections.observableArrayList()

    init {
        creationUseCase.getAllLanguages()
                .observeOnFx()
                .subscribe { retrieved ->
                    languages.setAll(retrieved)
                }

        creationUseCase.getSourceRepos()
                .observeOnFx()
                .subscribe { retrieved ->
                    resources.setAll(retrieved)
                }
    }

    fun getResourceChildren() {
        creationUseCase.getResourceChildren(selectedResource)
                .observeOnFx()
                .doOnSuccess {
                    anthologyList.setAll(it)
                }
                .subscribe()
    }

    fun getBooks() {
        creationUseCase.getResourceChildren(selectedAnthology)
                .observeOnFx()
                .doOnSuccess {
                    bookList.setAll(it)
                }
                .subscribe()
    }

    fun createProject() {
        creationUseCase
                .newProject(
                        Collection(
                                selectedBook.sort,
                                selectedBook.slug,
                                "project",
                                selectedBook.titleKey,
                                selectedBook.resourceContainer
                        )
                )
                .flatMapCompletable {
                    creationUseCase.updateSource(it, selectedBook)
                }
                .subscribe()
    }
}