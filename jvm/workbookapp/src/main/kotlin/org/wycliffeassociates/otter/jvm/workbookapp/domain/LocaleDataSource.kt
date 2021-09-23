package org.wycliffeassociates.otter.jvm.workbookapp.domain

import org.wycliffeassociates.otter.common.persistence.ILocaleDataSource
import java.util.*
import javax.inject.Inject

class LocaleDataSource @Inject constructor() : ILocaleDataSource {
    override fun getSupportedLocales(): List<String> {
        return javaClass.getResourceAsStream("/languages.properties").use {
            it?.let {
                val props = Properties()
                props.load(it)
                props.keys.map { it.toString() }
            } ?: listOf()
        }
    }

    override fun getDefaultLocale(): String {
        return Locale.getDefault().language
    }
}
