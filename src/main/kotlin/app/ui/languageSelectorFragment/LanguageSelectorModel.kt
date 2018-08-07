package app.ui.languageSelectorFragment

import data.model.Language
import app.widgets.filterableComboBox.ComboBoxSelectionItem

/**
 * This class holds a list of selected languages and tracks which one is the preferred language.
 *
 * @author Caleb Benedick
 */
class LanguageSelectorModel(private val languages : List<Language>) {
    var preferredSelection : Language? = null
    val selectedData : MutableList<Language> = mutableListOf()

    fun selectionItemToLanguage(languageItem: ComboBoxSelectionItem) : Language? {
        val language : Language

        try {
            language = languages.first { it.toTextView() == languageItem.labelText }
        } catch(e: NoSuchElementException) {
            return null
        }

        return language
    }
}
