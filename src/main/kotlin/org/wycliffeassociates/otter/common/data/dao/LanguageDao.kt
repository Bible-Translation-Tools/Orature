package org.wycliffeassociates.otter.common.data.dao

import org.wycliffeassociates.otter.common.data.model.Language
import io.reactivex.Observable
import java.util.*

interface LanguageDao : Dao<Language> {
    fun getGatewayLanguages(): Observable<List<Language>>
}