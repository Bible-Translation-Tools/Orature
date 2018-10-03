package org.wycliffeassociates.otter.jvm.app.ui.profilelanguageselection.model

import org.wycliffeassociates.otter.common.data.model.Language

class ProfileLanguageSelectionModel {

    // Temp list of languages to demo
    val languageVals = listOf<Language>()

    // Selected languages for profile
    val selectedTargetLanguages = mutableListOf<Language>()
    val selectedSourceLanguages = mutableListOf<Language>()
    var preferredTargetLanguage : Language? = null
    var preferredSourceLanguage : Language? = null
}