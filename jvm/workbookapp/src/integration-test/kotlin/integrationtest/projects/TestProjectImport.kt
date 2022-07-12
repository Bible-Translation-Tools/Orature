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
package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ContentType.BODY
import org.wycliffeassociates.otter.common.data.primitives.ContentType.META
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TEXT
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TITLE
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider

class TestProjectImport {

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()

    private val sourceMetadata = ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true, ""),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        "",
        File(".")
    )

    private val ulbTargetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe")
    )

    private val tnTargetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe"),
        identifier = "tn",
        version = sourceMetadata.version,
        type = ContainerType.Help
    )

    private val project = Collection(
        1,
        "rev",
        "rev",
        "",
        null
    )

    private val ulbProjectDir = db.directoryProvider.getProjectDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val ulbSourceDir = db.directoryProvider.getProjectSourceDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val ulbAudioDir = db.directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        ulbTargetMetadata,
        project
    )

    private val tnProjectDir = db.directoryProvider.getProjectDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    private val tnSourceDir = db.directoryProvider.getProjectSourceDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    private val tnAudioDir = db.directoryProvider.getProjectAudioDirectory(
        sourceMetadata,
        tnTargetMetadata,
        project
    )

    @Test
    fun ulb() {
        // Note that the total number of content entries is 31507
        // but, as verses are not derived by default, only in the presense of takes,
        // chapters 2-22 of Revelation are unchunked. Chapter 1 contains recordings, and thus is
        // derived verse by verse.
        db.import("en-x-demo1-ulb-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31124
                    ),
                    collections = 1279,
                    links = 0
                )
            )

        Assert.assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }

    @Test
    fun ulbDirectory() {
        db.import("en-x-demo1-ulb-rev.zip", unzip = true)
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31124
                    ),
                    collections = 1279,
                    links = 0
                )
            )

        Assert.assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }

    @Test
    fun tnHelps() {
        db.import("en-x-demo1-tn-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        META to 1211,
                        TEXT to 31124,
                        TITLE to 82025,
                        BODY to 79240
                    ),
                    collections = 1279,
                    links = 158796
                )
            )

        Assert.assertEquals(true, tnProjectDir.resolve("manifest.yaml").exists())
        Assert.assertEquals(true, tnSourceDir.resolve("en_ulb.zip").exists())
        Assert.assertEquals(true, tnSourceDir.resolve("en_tn-master.zip").exists())
        Assert.assertEquals(
            true,
            tnAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3
        )
    }

    @Test
    fun importContributorInfo() {
        val fileName = "en-x-demo1-ulb-rev.zip"
        val expectedContributors = 2

        val importPath = javaClass.classLoader.getResource("resource-containers/$fileName").file
        ResourceContainer.load(File(importPath)).use {
            assertEquals(expectedContributors, it.manifest.dublinCore.contributor.size)
        }
        // import resource container with 2 contributors
        db.import(fileName)

        val projectManifest = ulbProjectDir.resolve("manifest.yaml")
        assertTrue(projectManifest.exists())

        val projectContributors = ResourceContainer.load(ulbProjectDir).use {
            it.manifest.dublinCore.contributor.toList()
        }

        assertEquals(expectedContributors, projectContributors.size)
    }
}
