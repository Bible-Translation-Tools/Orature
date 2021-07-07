package org.wycliffeassociates.otter.jvm.workbookapp.ui


enum class OtterLocale(var slug: String, var language: String) {
    ENGLISH("en", "English"),
    SPANISH_LATIN_AMERICA("es_419", "Spanish (Latin America)"),
    FRENCH("fr", "French");

    companion object {
        private val map = values().associateBy { it.slug.toLowerCase() }

        fun of(slug: String) = map[slug.toLowerCase()] ?: ENGLISH
    }
}
