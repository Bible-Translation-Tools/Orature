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
import integrationtest.enUlbTestMetadata
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.CueChunk
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.ProjectMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.Mp3ProjectExporter
import org.wycliffeassociates.resourcecontainer.ResourceContainer

class TestProjectExport {
    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject lateinit var exportBackupUseCase: Provider<BackupProjectExporter>
    @Inject lateinit var exportMp3UseCase: Provider<Mp3ProjectExporter>
    @Inject lateinit var workbookRepository: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val db = dbEnvProvider.get()
    private lateinit var outputDir: File

    private lateinit var workbook: Workbook
    private lateinit var projectFilesAccessor: ProjectFilesAccessor

    private val sourceMetadata = enUlbTestMetadata.copy(
        path = getResource("resource-containers/en_ulb.zip")
    )

    private val targetMetadata = enUlbTestMetadata.copy(
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
    private val projectMetadata = ProjectMetadata(targetMetadata)

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
    fun exportMp3ProjectWithMetadata() {
        val testTake = createTestWavFile()
        val take = Take(
            "chapter-1",
            testTake,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select a take to be included when export
        workbook.target.chapters.blockingFirst().audio.selectTake(take)

        val result = exportMp3UseCase.get()
            .export(outputDir, projectMetadata, workbook, null)
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

    private fun createTestWavFile(): File {
        val testFile = File.createTempFile("test-take", "wav")
            .apply { deleteOnExit() }

        val wav = WavFile(
            testFile,
            DEFAULT_CHANNELS,
            DEFAULT_SAMPLE_RATE,
            DEFAULT_BITS_PER_SAMPLE,
            WavMetadata(listOf(CueChunk()))
        )
        WavOutputStream(wav).use {
            for (i in 0 until 4) {
                it.write(i)
            }
        }
        wav.update()
        return testFile
    }

    private fun getResource(name: String): File {
        return File(javaClass.classLoader.getResource("resource-containers/en_ulb.zip").file)
    }
}
