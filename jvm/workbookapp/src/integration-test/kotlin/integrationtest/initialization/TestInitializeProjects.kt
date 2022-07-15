/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package integrationtest.initialization

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeProjects
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeProjects {

    @Inject
    lateinit var initProjectsProvider: Provider<InitializeProjects>

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var env: DatabaseEnvironment

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val sourceMetadata = ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true, "Europe"),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        "",
        File(".")
    )

    private val targetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe")
    )

    private val project = Collection(
        1,
        "rev",
        "rev",
        "",
        null
    )

    @Test
    fun testInitializeProjects() {
        prepareInitialProject()

        val testSub = TestObserver<Completable>()
        val init = initProjectsProvider.get()
        init
            .exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, env.db.installedEntityDao.fetchVersion(init))

        env.assertRowCounts(
            RowCount(
                contents = mapOf(
                    ContentType.META to 1211,
                    ContentType.TEXT to 31124
                ),
                collections = 1279,
                links = 0
            )
        )
    }

    private fun prepareInitialProject() {
        val targetDir = directoryProvider.getProjectDirectory(
            sourceMetadata,
            targetMetadata,
            project
        )
        env.unzipProject("en-x-demo1-ulb-rev.zip", targetDir)
    }
}
