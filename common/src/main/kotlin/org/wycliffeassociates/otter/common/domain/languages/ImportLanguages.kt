package org.wycliffeassociates.otter.common.domain.languages

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository

// Imports from langnames.json
class ImportLanguages(val inputStream: InputStream, val languageRepo: ILanguageRepository) {
    fun import(): Completable {
        return Completable.fromCallable {
            val mapper = ObjectMapper(JsonFactory())
            mapper.registerModule(KotlinModule())
            val languages = inputStream.bufferedReader().use {
                mapper.readValue(it, Array<Door43Language>::class.java)
            }
            languageRepo.insertAll(languages.toList().map { it.toLanguage() }).blockingGet()
        }.subscribeOn(Schedulers.io())
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Door43Language(
    val pk: Int, // id
    val lc: String, // slug
    val ln: String, // name
    val ld: String, // direction
    val gw: Boolean, // isGateway
    val ang: String // anglicizedName
) {
    fun toLanguage(): Language = Language(lc, ln, ang, ld, gw)
}
