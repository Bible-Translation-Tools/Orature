package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model.ProjectCreationModel
import tornadofx.*

class ProjectCreationViewModel : ItemViewModel<ProjectCreationModel>(ProjectCreationModel()) {

    var sourceLanguage = bind(ProjectCreationModel::sourceLanguage, true)
    var targetLanguage = bind(ProjectCreationModel::targetLanguage, true)

    val collectionList = item.collectionList
    val languagesList = item.languages
    val selectedLanguageProjects = item.selectedLanguageProjectsProperty

    fun doOnUserSelection(selectedCollection: Collection) = item.doOnUserSelection(selectedCollection, this.workspace)
    fun goBack(wizard: Wizard) = item.goBack(wizard)
    fun getRootSources() = bind(ProjectCreationModel::getRootSources)
}