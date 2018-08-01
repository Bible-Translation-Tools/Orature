package persistence.data

import data.model.Language

class LanguageStore {
    companion object {
        val languages: List<Language> = listOf(
            Language(1, "en", "English", true, "English"),
            Language(2, "es", "Espanol", true, "Spanish"),
            Language(3, "fr", "Français", false, "French"),
            Language(4, "cmn", "官话", true, "Mandarin Chinese"),
            Language(5, "ar", "العَرَبِيَّة", false, "Arabic"),
            Language(6, "gln", "Glenn", true, "Glenn")

        )

        fun getLanguageForSlug(slug: String): Language {
            return languages.filter { it.slug == slug }.first()
        }

        fun getById(id: Int): Language {
            return languages[id - 1]
        }
    }

}