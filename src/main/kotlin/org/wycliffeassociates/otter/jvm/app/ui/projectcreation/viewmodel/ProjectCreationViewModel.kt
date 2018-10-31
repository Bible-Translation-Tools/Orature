package org.wycliffeassociates.otter.jvm.app.ui.projectcreation.viewmodel

import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.jvm.app.ui.projectcreation.model.ProjectCreationModel
import tornadofx.*

class ProjectCreationViewModel : ItemViewModel<ProjectCreationModel>(ProjectCreationModel()) {

    var sourceLanguage = bind(ProjectCreationModel::sourceLanguage)
    var targetLanguage = bind(ProjectCreationModel::targetLanguage)

    val collectionList = item.collectionList
    val languagesList = item.languages
    val selectedLanguageProjects = item.selectedLanguageProjectsProperty

    fun doOnUserSelection(selectedCollection: Collection) = item.doOnUserSelection(selectedCollection, this.workspace)
    fun goBack(wizard: Wizard) = item.goBack(wizard)
    fun getRootSources() = bind(ProjectCreationModel::getRootSources)
    fun reset() {
        sourceLanguage.value = null
        targetLanguage.value = null
        item.reset()
    }
}
