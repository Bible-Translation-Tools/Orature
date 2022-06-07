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

import integrationtest.createTestWavFile
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.enUlbTestMetadata
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.Mp3ProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestProjectExport {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    @Inject
    lateinit var exportBackupProvider: Provider<BackupProjectExporter>
    @Inject
    lateinit var exportMp3Provider: Provider<Mp3ProjectExporter>
    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val db = dbEnvProvider.get()
    private lateinit var outputDir: File

    private lateinit var workbook: Workbook
    private lateinit var projectFilesAccessor: ProjectFilesAccessor

    private val sourceMetadata = enUlbTestMetadata

    private val targetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe")
    )

    private val targetCollection = Collection(
        1,
        "rev",
        "rev",
        "",
        targetMetadata
    )

    @Before
    fun setUp() {
        db.import("en-x-demo1-ulb-rev.zip")
        workbook = workbookRepository.getProjects().blockingGet().single()
        projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            targetMetadata,
            targetCollection
        )
        outputDir = createTempDirectory("orature-export-test").toFile()
    }

    @After
    fun cleanUp() {
        outputDir.deleteRecursively()
    }

    @Test
    fun exportOratureProjectWithMetadata() {
        val result = exportBackupProvider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        assertNotNull(file)

        val exportedContributorList = ResourceContainer.load(file!!).use {
            it.manifest.dublinCore.contributor.toList()
        }
        assertTrue(exportedContributorList.isNotEmpty())
    }

    @Test
    fun exportMp3ProjectWithMetadata() {
        val testTake = createTestWavFile(directoryProvider.tempDirectory)
        val take = Take(
            "chapter-1",
            testTake,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select a take to be included when export
        workbook.target.chapters.blockingFirst().audio.selectTake(take)

        val result = exportMp3Provider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        val exportedChapter = outputDir.walk().firstOrNull { it.extension == "mp3" }
        val contributorCount = AudioFile(exportedChapter!!).metadata.artists().size

        assertNotNull("Exported file not found.", exportedChapter)
        assertEquals(
            "Exported file metadata does not match contributors info.",
            projectFilesAccessor.getContributorInfo().size,
            contributorCount
        )
    }
}