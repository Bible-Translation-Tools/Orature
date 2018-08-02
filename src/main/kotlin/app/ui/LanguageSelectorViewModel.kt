package app.ui

import data.model.Language
import io.reactivex.subjects.PublishSubject
import widgets.Chip
import widgets.ComboBoxSelectionItem

/**
 * This class is used by the LanguageSelector fragment as the go-between for the view and the model, taking in
 * PublishSubjects that track user interaction in the Fragment and then updating the data stored in the model.
 *
 * @author Caleb Benedick and Kimberly Horton
 *
 * @param updateSelectedLanguages a PublishSubject that takes in a new selected language and updates the list stored
 * in the model
 * @param preferredLanguage a PublishSubject that takes in a new preferred language and updates the value stored in
 * the model
 */
class LanguageSelectorViewModel(
        private val updateSelectedLanguages: PublishSubject<Language>,
        private val preferredLanguage: PublishSubject<Language>,
        languages: List<Language>
) {

    private val model = LanguageSelectorModel(languages)

    fun addNewValue(selection: ComboBoxSelectionItem) {
        val language = model.selectionItemToLanguage(selection)
        if (language != null && !model.selectedData.contains(language)) {
            model.selectedData.add(0, language)
            updateSelectedLanguages.onNext(language)
            setPreferredLanguage(language)
        }
    }

    fun newPreferredLanguage(chipLanguage: String) {
        setPreferredLanguage(model.selectedData.first { it.slug == chipLanguage })
    }

    fun removeLanguage(chipLanguage: String) {
        val language = model.selectedData.first { it.slug == chipLanguage }
        model.selectedData.remove(language)
        updateSelectedLanguages.onNext(language)
        if (model.selectedData.isNotEmpty()) {
            if (language == model.preferredSelection) {
                setPreferredLanguage(model.selectedData.first())
            } else {
                setPreferredLanguage(model.preferredSelection)
            }
        } else {
            setPreferredLanguage(null)
        }
    }

    private fun setPreferredLanguage(language: Language?) {
        // we still want to notify the profile that there is no selected preferred language somehow?
        if (language != model.preferredSelection) {
            model.preferredSelection = language
            language?.let { preferredLanguage.onNext(it) }
        }
    }

}
