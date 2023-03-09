package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.versification.ParatextVersification
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File
import javax.inject.Inject

class VersificationRepository @Inject constructor(
    database: AppDatabase,
    private val directoryProvider: IDirectoryProvider
): IVersificationRepository {

    private val logger = LoggerFactory.getLogger(LanguageRepository::class.java)

    private val versificationDao = database.versificationDao

    override fun getVersification(slug: String): Maybe<Versification> {
        return Maybe.fromCallable {
            directoryProvider.versificationDirectory.mkdirs()
            val vrsFileName = versificationDao.fetchVersificationFile(slug)
            val vrsFile = File(directoryProvider.versificationDirectory, vrsFileName)
            val mapper = ObjectMapper(JsonFactory())
            mapper.registerModule(KotlinModule())
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            val versification = mapper.readValue(vrsFile, ParatextVersification::class.java)
            if (versification is Versification) Maybe.just(versification as Versification) else Maybe.empty()
        }
            .flatMap { it }
            .subscribeOn(Schedulers.io())
    }

    override fun insertVersification(slug: String, path: File): Completable {
        return Single.fromCallable {
            versificationDao.insertVersification(slug, path.name)
        }.ignoreElement()
            .subscribeOn(Schedulers.io())

    }
}