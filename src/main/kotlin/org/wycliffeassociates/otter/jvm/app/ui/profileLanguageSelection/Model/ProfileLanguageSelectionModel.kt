package org.wycliffeassociates.otter.jvm.app.ui.profileLanguageSelection.Model

import data.model.Language

class ProfileLanguageSelectionModel {

    // Temp list of languages to demo
    val languageVals = listOf(
            Language(0, "ENG", "English", true, "0"),
            Language(0, "MAN", "Mandarin", true, "1"),
            Language(0, "ESP", "Spanish", true, "2"),
            Language(0, "ARA", "Arabic", true, "3"),
            Language(0, "RUS", "Russian", true, "4"),
            Language(0, "AAR", "Afrikaans", true, "5"),
            Language(0, "HEB", "Hebrew", true, "6"),
            Language(0, "JAP", "Japanese", true, "7"),
            Language(0, "FRN", "French", true, "8")
    )

    // Selected languages for profile
    val selectedTargetLanguages = mutableListOf<Language>()
    val selectedSourceLanguages = mutableListOf<Language>()
    var preferredTargetLanguage : Language? = null
    var preferredSourceLanguage : Language? = null
}