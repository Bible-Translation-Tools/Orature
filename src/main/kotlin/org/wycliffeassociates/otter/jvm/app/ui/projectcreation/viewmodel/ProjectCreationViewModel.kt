package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel

import javafx.beans.property.SimpleBooleanProperty
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model.ProjectCreationModel
import tornadofx.*

class ProjectCreationViewModel : ItemViewModel<ProjectCreationModel>(ProjectCreationModel()) {

    var sourceLanguage = bind(ProjectCreationModel::sourceLanguageProperty, true)
    var targetLanguage = bind(ProjectCreationModel::targetLanguageProperty, true)

    val resourceListProperty = bind(ProjectCreationModel::resources)
    val collectionList = item.collectionList
    val languagesList = item.languages
    val anthologyList = item.anthologyList
    val bookList = item.bookList


    fun checkLevel(selectedCollection: Collection) = item.checkLevel(selectedCollection, this.workspace)
    fun createProject() = bind(ProjectCreationModel::createProject)
    fun getSourceRepos() = bind(ProjectCreationModel::getSourceRepos)
}