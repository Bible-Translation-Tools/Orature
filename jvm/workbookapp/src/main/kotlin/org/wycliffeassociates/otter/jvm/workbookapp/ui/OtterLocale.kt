package org.wycliffeassociates.otter.jvm.workbookapp.ui

import java.io.File
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
        private val bundlePrefix = "Messages_"
        private val bundleExtension = ".properties"

        private var defaultLocale: Locale = Locale.getDefault()
        private var actualLocale: Locale = Locale.ENGLISH
        private val supportedLocales = getSupportedLocales()

        private val localeAlternatives = mutableMapOf(
            "es_MX" to Locale("es", "419"),
            "es_AR" to Locale("es", "419")
        )

        private fun getSupportedLocales(): MutableList<Locale> {
            val url = this.javaClass.getResource("/Messages_en.properties")
            return url?.let {
                val path = it.path
                val parentDir = File(path).parentFile
                val files = parentDir.listFiles { file ->
                    file.name.startsWith(bundlePrefix) && file.name.endsWith(bundleExtension)
                }
                files?.map(this::fileToLocale)?.toMutableList()
            } ?: mutableListOf()
        }

        private fun fileToLocale(file: File): Locale {
            val name = file.nameWithoutExtension.replace(bundlePrefix, "")
            val parts = name.split("_", limit = 2)
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
