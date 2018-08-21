package org.wycliffeassociates.otter.jvm.app.ui.profileLanguageSelection.ViewModel

import org.wycliffeassociates.otter.jvm.app.ui.profileLanguageSelection.Model.ProfileLanguageSelectionModel
import data.model.Language
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty

class ProfileLanguageSelectionViewModel {
    private val model = ProfileLanguageSelectionModel()

    // used in Language selector and subscribed to in the view to update the model's lists
    val updateSelectedTargets = PublishSubject.create<Language>()
    val updateSelectedSources = PublishSubject.create<Language>()
    val updatePreferredTarget = PublishSubject.create<Language>()
    val updatePreferredSource = PublishSubject.create<Language>()

    val isNextAvailable = SimpleBooleanProperty(false)

    fun getTargetLanguageOptions() : List<Language> {
        return model.languageVals //normally would be non gateway languages
    }

    fun getSourceLanguageOptions() : List<Language> {
        return model.languageVals //normally would be gateway languages
    }

    fun updateSelectedTargetLanguages(language : Language) {
        if(!model.selectedTargetLanguages.remove(language)) {
            model.selectedTargetLanguages.add(language)
        } else if (model.selectedTargetLanguages.isEmpty()) {
            model.preferredTargetLanguage = null
        }
        languagesSelected()
    }

    fun updateSelectedSourceLanguages(language : Language) {
        if(!model.selectedSourceLanguages.remove(language)) {
            model.selectedSourceLanguages.add(language)
        } else if (model.selectedSourceLanguages.isEmpty()) {
            model.preferredSourceLanguage = null
        }
        languagesSelected()
    }

    fun updatePreferredTargetLanguage(language : Language) {
        model.preferredTargetLanguage = language
    }

    fun updatePreferredSourceLanguage(language : Language) {
        model.preferredSourceLanguage = language
    }

    private fun languagesSelected() {
        isNextAvailable.set(model.selectedSourceLanguages.isNotEmpty() && model.selectedTargetLanguages.isNotEmpty())
    }

}
