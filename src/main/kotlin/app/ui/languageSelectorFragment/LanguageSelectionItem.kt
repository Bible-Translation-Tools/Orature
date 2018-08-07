package app.ui.languageSelectorFragment

import data.model.Language
import app.widgets.filterableComboBox.ComboBoxSelectionItem

/**
 * This class takes in a language object, creates a string to be displayed in the ComboBox, and creates a list
 * containing the language name, slug, and anglicized name.
 *
 * @author Caleb Benedick
 *
 * @param language a language object to be converted to a ComboBoxSelectionItem
 */
class LanguageSelectionItem(language: Language) : ComboBoxSelectionItem {
    override val labelText = language.toTextView()
    override val filterText = listOf(language.name, language.slug, language.anglicizedName)
}

/**
 * This function converts a language into a string to be displayed.
 *
 * @author Caleb Benedick
 *
 * @return a string displaying the language slug followed by the name in parenthesis
 */
fun Language.toTextView() : String {
    return "${this.slug.toUpperCase()} (${this.name.capitalize()})"
}
