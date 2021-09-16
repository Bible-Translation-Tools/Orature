package org.wycliffeassociates.otter.common.domain

import org.wycliffeassociates.otter.common.data.primitives.Language

interface ILocaleLanguage {
    val actualLanguage: Language?
    val defaultLanguage: Language?
    val supportedLanguages: List<Language>
}
