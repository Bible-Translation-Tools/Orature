package org.wycliffeassociates.otter.jvm.workbookapp.domain

import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.domain.ILocaleLanguage
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import java.util.*
import javax.inject.Inject

class LocaleLanguage @Inject constructor(
    private val appPrefRepo: IAppPreferencesRepository,
    private val langRepo: ILanguageRepository
) : ILocaleLanguage {

    override val actualLanguage: Language?
        get() = actualLanguage()

    override val defaultLanguage: Language?
        get() = defaultLanguage()

    override val supportedLanguages: List<Language>
        get() = supportedLanguages()

    private fun actualLanguage(): Language? {
        val language = getLanguageFromPrefs()
        return when {
            supportedLanguages.contains(language) -> language
            else -> defaultLanguage
        }
    }

    private fun defaultLanguage(): Language? {
        val systemLocale = Locale.getDefault()
        val systemLanguage = getLanguageBySlug(systemLocale.language)
        return when {
            supportedLanguages.contains(systemLanguage) -> systemLanguage
            else -> getLanguageBySlug("en")
        }
    }

    private fun supportedLanguages(): List<Language> {
        return javaClass.getResourceAsStream("/languages.properties").use {
            val props = Properties()
            it?.let {
                props.load(it)
                langRepo.getAll().blockingGet()
                    .filter {
                        props.keys.contains(it.slug)
                    }
                    .sortedBy { it.slug }
            } ?: listOf()
        }
    }

    private fun getLanguageBySlug(slug: String): Language? {
        return langRepo.getBySlug(slug)
            .flatMapMaybe { Maybe.just(it) }
            .onErrorComplete()
            .blockingGet()
    }

    private fun getLanguageFromPrefs(): Language? {
        return appPrefRepo.localeLanguage()
            .onErrorComplete()
            .blockingGet()
    }
}
