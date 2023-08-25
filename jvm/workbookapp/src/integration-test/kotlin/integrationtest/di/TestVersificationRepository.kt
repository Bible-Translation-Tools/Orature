/**
 * Copyright (C) 2020-2023 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        val mapper = ObjectMapper().registerKotlinModule()
        val versification = mapper.readValue(vrsFile, ParatextVersification::class.java)
        return Maybe.just(versification)
    }

    override fun insertVersification(slug: String, path: File): Completable {
        return Completable.complete()
    }
}