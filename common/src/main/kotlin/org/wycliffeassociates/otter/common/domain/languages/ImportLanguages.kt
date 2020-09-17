package org.wycliffeassociates.otter.common.domain.languages

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import java.io.InputStream

// Imports from langnames.json
class ImportLanguages(val inputStream: InputStream, val languageRepo: ILanguageRepository) {
    private val logger = LoggerFactory.getLogger(ImportLanguages::class.java)
    fun import(): Completable {
        return Completable
            .fromCallable {
                val mapper = ObjectMapper(JsonFactory())
                mapper.registerModule(KotlinModule())
                val languages = inputStream.bufferedReader().use {
                    mapper.readValue(it, Array<Door43Language>::class.java)
                }
                languageRepo.insertAll(languages.toList().map { it.toLanguage() }).blockingGet()
            }
            .doOnError { e ->
                logger.error("Error in ImportLanguages", e)
            }
            .subscribeOn(Schedulers.io())
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
