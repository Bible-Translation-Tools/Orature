package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model.ProjectCreationModel
import tornadofx.*

class ProjectCreationViewModel : ItemViewModel<ProjectCreationModel>(ProjectCreationModel()) {

    var sourceLanguage = bind(ProjectCreationModel::sourceLanguageProperty, true)
    var targetLanguage = bind(ProjectCreationModel::targetLanguageProperty, true)
    val creaionDepthProperty = bind(ProjectCreationModel::creationDepth)

    val collectionList = item.collectionList
    val languagesList = item.languages

    fun checkLevel(selectedCollection: Collection) = item.checkLevel(selectedCollection, this.workspace)
    fun goBack(wizard: Wizard) = item.getPreviousCollections(wizard)
    fun getSourceRepos() = bind(ProjectCreationModel::getSourceRepos)
    fun getDepth(selectedCollection: Collection) = item.getDepth(selectedCollection)
}