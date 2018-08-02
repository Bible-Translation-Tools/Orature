package app.ui

import data.model.Language
import org.junit.Test

class TestLanguageSelectorModel {
    private val languageVals = listOf(
            Language(0, "ENG", "English", true, "0"),
            Language(0, "MAN", "Mandarin", true, "1"),
            Language(0, "ESP", "Spanish", true, "2"),
            Language(0, "ARA", "Arabic", true, "3"),
            Language(0, "RUS", "Russian", true, "4"),
            Language(0, "AAR", "Afrikaans", true, "5"),
            Language(0, "HEB", "Hebrew", true, "6"),
            Language(0, "JAP", "Japanese", true, "7")
    )

    @Test
    fun interfaceToLanguageSafeTest() {
        val languageSelectorModel = LanguageSelectorModel(languageVals)
        // check and see if the correct language is returned
        val expected = languageVals[0]
        val actual = LanguageSelectionItem(languageVals[0])
        assert(expected == languageSelectorModel.selectionItemToLanguage(actual))
    }

    @Test
    fun interfaceToLanguageUnsafeTest() {
        val languageSelectorModel = LanguageSelectorModel(languageVals)
        // check and see if the response occurs when no language is found
        val expected = null
        val actual = languageSelectorModel.selectionItemToLanguage(
                LanguageSelectionItem(Language(3, "123", "123", true, "123"))
        )
        assert(expected == actual)
    }
}
