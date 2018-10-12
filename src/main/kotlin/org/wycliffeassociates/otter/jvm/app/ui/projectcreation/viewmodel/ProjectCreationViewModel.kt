package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model.ProjectCreationModel
import tornadofx.*

class ProjectCreationViewModel : ItemViewModel<ProjectCreationModel>(ProjectCreationModel()) {

    var sourceLanguage = bind(ProjectCreationModel::sourceLanguageProperty, true)
    var targetLanguage = bind(ProjectCreationModel::targetLanguageProperty, true)

    var selectedResourceProperty = bind(ProjectCreationModel::selectedResource, true)
    var selectedAnthologyProperty = bind(ProjectCreationModel::selectedAnthology, true)
    val selectedBookProperty = bind(ProjectCreationModel::selectedBook, true)
    val resourceListProperty = bind(ProjectCreationModel::resources)
    val languagesList = item.languages
    val anthologyList = item.anthologyList
    val bookList = item.bookList

    val allPagesComplete = SimpleBooleanProperty(false)
    val resourceSelected = SimpleBooleanProperty(false)
    val anthologySelected = SimpleBooleanProperty(false)

    init {
        selectedBookProperty.onChange {
            if (it != null) {
                allPagesComplete.set(true)
            }
        }
        selectedResourceProperty.onChange {
            if(it != null) {
                resourceSelected.set(true)
            }
        }

        selectedAnthologyProperty.onChange {
            if(it != null) {
                anthologySelected.set(true)
            }
        }
    }

    fun getResourceChildren() = bind(ProjectCreationModel::getResourceChildren)
    fun getBooks() = bind(ProjectCreationModel::getBooks)
    fun createProject() = bind(ProjectCreationModel::createProject)
}