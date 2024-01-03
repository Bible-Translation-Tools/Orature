package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Maybe
import org.wycliffeassociates.otter.common.domain.versification.Versification
import java.io.File

interface IVersificationRepository {
    fun getVersification(slug: String): Maybe<Versification>

    fun insertVersification(
        slug: String,
        path: File,
    ): Completable
}
