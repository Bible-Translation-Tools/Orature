package org.wycliffeassociates.otter.common.persistence

interface ILocaleDataSource {
    fun getSupportedLocales(): List<String>
    fun getDefaultLocale(): String
}
