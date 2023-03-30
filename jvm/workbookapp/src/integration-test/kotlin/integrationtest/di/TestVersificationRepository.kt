package integrationtest.di

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.reactivex.Completable
import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.domain.versification.ParatextVersification
import org.wycliffeassociates.otter.common.domain.versification.Versification
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import java.io.File
import javax.inject.Inject

class TestVersificationRepository @Inject constructor(): IVersificationRepository {
    override fun getVersification(slug: String): Maybe<Versification> {
        val vrsFile = ClassLoader.getSystemResourceAsStream("versification/ulb_versification.json")
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val versification = mapper.readValue(vrsFile, ParatextVersification::class.java)
        return Maybe.just(versification)
    }

    override fun insertVersification(slug: String, path: File): Completable {
        return Completable.complete()
    }
}