package org.wycliffeassociates.otter.jvm.workbookapp.ui

import java.util.*


class OtterLocale private constructor(
    private val defaultLocale: Locale,
    private val actualLocale: Locale,
    private val supportedLocales: List<Locale>,
    private val localeAlternatives: Map<String, Locale>
) {

    fun getSupportedLocales(): List<Locale> {
        return supportedLocales
    }

    fun getDefaultLocale(): Locale {
        val languageLocale = Locale(defaultLocale.language)
        val localeStr = defaultLocale.toString()
        return when {
            supportedLocales.contains(defaultLocale) -> defaultLocale
            supportedLocales.contains(languageLocale) -> languageLocale
            localeAlternatives.containsKey(localeStr) -> localeAlternatives[localeStr]!!
            else -> Locale.ENGLISH
        }
    }

    fun getActualLocale(): Locale {
        val languageLocale = Locale(actualLocale.language)
        val localeStr = actualLocale.toString()
        return when {
            supportedLocales.contains(actualLocale) -> actualLocale
            supportedLocales.contains(languageLocale) -> languageLocale
            localeAlternatives.containsKey(localeStr) -> localeAlternatives[localeStr]!!
            else -> getDefaultLocale()
        }
    }

    class Builder {
        private var defaultLocale: Locale = Locale.getDefault()
        private var actualLocale: Locale = Locale.ENGLISH
        private val supportedLocales = getSupportedLocales()

        private val localeAlternatives = mutableMapOf(
            "es_MX" to Locale("es", "419"),
            "es_AR" to Locale("es", "419")
        )

        private fun getSupportedLocales(): MutableList<Locale> {
            return this.javaClass.getResourceAsStream("/locales.properties").use {
                val props = Properties()
                it?.let {
                    props.load(it)
                    props.keys.map(this::propToLocale)
                        .sortedBy { it.language }
                        .toMutableList()
                } ?: mutableListOf()
            }
        }

        private fun propToLocale(prop: Any): Locale {
            val key = prop.toString()
            val parts = key.split("_", limit = 2)
            val language = parts[0]
            val country = parts.getOrNull(1) ?: ""
            return Locale(language, country)
        }

        fun setSupportedLocales(locales: List<Locale>): Builder {
            supportedLocales.clear()
            supportedLocales.addAll(locales)
            return this
        }

        fun setLocaleAlternatives(locales: Map<String, Locale>): Builder {
            localeAlternatives.clear()
            localeAlternatives.putAll(locales)
            return this
        }

        fun setDefaultLocale(locale: Locale): Builder {
            defaultLocale = locale
            return this
        }

        fun setActualLocale(locale: Locale): Builder {
            actualLocale = locale
            return this
        }

        fun build(): OtterLocale {
            return OtterLocale(
                defaultLocale,
                actualLocale,
                supportedLocales,
                localeAlternatives
            )
        }
    }
}
