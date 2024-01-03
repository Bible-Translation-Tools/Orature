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
package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ContentType.META
import org.wycliffeassociates.otter.common.data.primitives.ContentType.TEXT
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
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

    private val sourceMetadata =
        ResourceMetadata(
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
            File("."),
        )

    private val ulbTargetMetadata =
        sourceMetadata.copy(
            creator = "Orature",
            language = Language("en-x-demo1", "", "", "", true, "Europe"),
        )

    private val tnTargetMetadata =
        sourceMetadata.copy(
            creator = "Orature",
            language = Language("en-x-demo1", "", "", "", true, "Europe"),
            identifier = "tn",
            version = sourceMetadata.version,
            type = ContainerType.Help,
        )

    private val project =
        Collection(
            1,
            "rev",
            "rev",
            "",
            null,
        )

    private val ulbProjectDir =
        db.directoryProvider.getProjectDirectory(
            sourceMetadata,
            ulbTargetMetadata,
            project,
        )

    private val ulbSourceDir =
        db.directoryProvider.getProjectSourceDirectory(
            sourceMetadata,
            ulbTargetMetadata,
            project,
        )

    private val ulbAudioDir =
        db.directoryProvider.getProjectAudioDirectory(
            sourceMetadata,
            ulbTargetMetadata,
            project,
        )

    private val tnProjectDir =
        db.directoryProvider.getProjectDirectory(
            sourceMetadata,
            tnTargetMetadata,
            project,
        )

    private val tnSourceDir =
        db.directoryProvider.getProjectSourceDirectory(
            sourceMetadata,
            tnTargetMetadata,
            project,
        )

    private val tnAudioDir =
        db.directoryProvider.getProjectAudioDirectory(
            sourceMetadata,
            tnTargetMetadata,
            project,
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
                    contents =
                        mapOf(
                            META to 2378,
                            TEXT to 31124,
                        ),
                    collections = 2511,
                    links = 0,
                ),
            )

        assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3,
        )
    }

    @Test
    fun ulbDirectory() {
        db.import("en-x-demo1-ulb-rev.zip", unzip = true)
            .assertRowCounts(
                RowCount(
                    contents =
                        mapOf(
                            META to 2378,
                            TEXT to 31124,
                        ),
                    collections = 2511,
                    links = 0,
                ),
            )

        assertEquals(true, ulbProjectDir.resolve("manifest.yaml").exists())
        assertEquals(true, ulbSourceDir.resolve("en_ulb.zip").exists())
        assertEquals(
            true,
            ulbAudioDir.walkTopDown()
                .filter { it.extension == "wav" }
                .count() == 3,
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

        val projectContributors =
            ResourceContainer.load(ulbProjectDir).use {
                it.manifest.dublinCore.contributor.toList()
            }

        assertEquals(expectedContributors, projectContributors.size)
    }

    @Test
    fun importProjectWithCheckedChunks() {
        val projectToImport = createProjectWithCheckedChunks("en-x-demo1-ulb-rev.zip", CheckingStatus.PEER_EDIT)
        val result =
            db.importer
                .import(projectToImport)
                .blockingGet()

        assertEquals(ImportResult.SUCCESS, result)
        val takes =
            db.db
                .takeDao
                .fetchAll()

        assertTrue(takes.size >= 3)

        val checkedTakes =
            takes
                .map {
                    db.db.checkingStatusDao.fetchById(it.checkingFk)
                }
                .filter { it == CheckingStatus.PEER_EDIT }
                .size

        assertEquals(2, checkedTakes)
    }

    private fun createProjectWithCheckedChunks(
        projectFileName: String,
        checking: CheckingStatus,
    ): File {
        val file =
            File(
                javaClass.classLoader.getResource("resource-containers/$projectFileName").path,
            )
        return ResourceContainerBuilder(file)
            .addTake(1, TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, TEXT, 1, true, chapter = 2, start = 1, end = 1, checking = TakeCheckingState(checking, "test"))
            .addTake(3, TEXT, 1, true, chapter = 3, start = 1, end = 1, checking = TakeCheckingState(checking, "test"))
            .buildFile()
    }
}
