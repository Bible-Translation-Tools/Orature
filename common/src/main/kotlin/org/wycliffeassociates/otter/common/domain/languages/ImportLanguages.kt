package org.wycliffeassociates.otter.common.domain.languages

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import java.io.InputStream
import javax.inject.Inject

// Imports from langnames.json
class ImportLanguages @Inject constructor(val languageRepo: ILanguageRepository) {

    private val logger = LoggerFactory.getLogger(ImportLanguages::class.java)

    fun import(inputStream: InputStream): Completable {
        return Completable
            .fromCallable {
                val languages = mapLanguages(inputStream)
                languageRepo.insertAll(languages).blockingGet()
            }
            .doOnError { e ->
                logger.error("Error in ImportLanguages", e)
            }
            .subscribeOn(Schedulers.io())
    }

    fun updateRegions(inputStream: InputStream): Completable {
        return Completable
            .fromCallable {
                val languages = mapLanguages(inputStream)
                languageRepo.updateRegions(languages).blockingGet()
            }
            .doOnError { e ->
                logger.error("Error in updateRegions", e)
            }
            .subscribeOn(Schedulers.io())
    }

    private fun mapLanguages(inputStream: InputStream): List<Language> {
        val mapper = ObjectMapper(JsonFactory())
        mapper.registerModule(KotlinModule())
        val languages = inputStream.bufferedReader().use {
            mapper.readValue(it, Array<Door43Language>::class.java)
        }
        return languages.toList().map { it.toLanguage() }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class Door43Language(
    val pk: Int, // id
    val lc: String, // slug
    val ln: String, // name
    val ld: String, // direction
    val gw: Boolean, // isGateway
    val ang: String, // anglicizedName
    val lr: String // region
) {
    fun toLanguage(): Language = Language(lc, ln, ang, ld, gw, lr)
}
