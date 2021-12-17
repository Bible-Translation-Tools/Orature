/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject
import javax.inject.Provider

class TestProjectCreate {
    private val numberOfChaptersInHebrews: Int = 13
    private val numberOfVersesInHebrews: Int = 303
    private val numberOfResourcesInTn: Int = 157581
    private val numberOfResourcesInTnHebrews: Int = 33758

    @Inject
    lateinit var collectionRepo: ICollectionRepository
    @Inject
    lateinit var languageRepo: ILanguageRepository

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun derivativeLinksForBook() {
        val env = dbEnvProvider.get()
        env
            .import("en_ulb.zip")
            .assertRowCounts(RowCount(links = 0, derivatives = 0))

        env.createProject(
            sourceProject = env.getHebrewsSourceBook().blockingGet(),
            targetLanguage = env.getHebrewLanguage().blockingGet()
        )
        env.assertRowCounts(RowCount(links = 0, derivatives = numberOfChaptersInHebrews + numberOfVersesInHebrews))
    }

    @Test
    fun derivativeLinksForHelps() {
        val env = dbEnvProvider.get()
        env
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .assertRowCounts(RowCount(links = numberOfResourcesInTn, derivatives = 0))

        env.createProject(
            sourceProject = env.getHebrewsSourceBook().blockingGet(),
            targetLanguage = env.getHebrewLanguage().blockingGet()
        )

        env.assertRowCounts(
            RowCount(
                links = numberOfResourcesInTn,
                derivatives = numberOfResourcesInTnHebrews + numberOfChaptersInHebrews + numberOfVersesInHebrews
            )
        )
    }

    private fun DatabaseEnvironment.getHebrewsSourceBook() =
        collectionRepo.getSourceProjects().map { it.single { it.slug == "heb" } }.cache()

    private fun DatabaseEnvironment.getHebrewLanguage() =
        languageRepo.getBySlug("hbo").cache()
}
