package org.wycliffeassociates.otter.common.persistence

interface ILocaleDataStore {
    fun getSupportedLocales(): List<String>
    fun getDefaultLocale(): String
}
