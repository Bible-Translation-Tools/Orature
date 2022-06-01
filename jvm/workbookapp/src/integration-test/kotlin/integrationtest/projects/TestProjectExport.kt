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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.Mp3ProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.SourceProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestProjectExport {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject
    lateinit var exportSourceProvider: Provider<SourceProjectExporter>
    @Inject
    lateinit var exportBackupProvider: Provider<BackupProjectExporter>
    @Inject
    lateinit var exportMp3Provider: Provider<Mp3ProjectExporter>
    @Inject
    lateinit var workbookRepository: IWorkbookRepository
    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val db = dbEnvProvider.get()
    private lateinit var outputDir: File

    private lateinit var workbook: Workbook
    private lateinit var projectFilesAccessor: ProjectFilesAccessor

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

    @Test
    fun exportProjectAsSource() {
        val testTake = createTestWavFile()
        val take = Take(
            "chapter-take",
            testTake,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select a take to be included when export
        workbook.target.chapters.blockingFirst().audio.selectTake(take)
        AudioFile(testTake).apply {
            metadata.addCue(1, "marker-1")
            update()
        }

        val result = exportSourceProvider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        ResourceContainer.load(outputDir.listFiles().first()).use { rc ->
            assertEquals(1, rc.media?.projects?.size ?: 0)

            val files = getExportedFiles(rc)
            assertEquals(2, files.size)
            assertTrue(files.any { it.endsWith(".mp3") })
            assertTrue(files.any { it.endsWith(".cue") })
        }
    }

    private fun getExportedFiles(rc: ResourceContainer): Set<String> {
        val projectMediaPath = rc.media?.projects?.first()?.media?.first { it.identifier == "mp3" }?.chapterUrl
        val filePathInContainer = File(projectMediaPath).parentFile.invariantSeparatorsPath
        val files = rc.accessor.getInputStreams(filePathInContainer, listOf("mp3", "cue"))
        files.forEach { (name, ins) ->
            ins.close()
        }
        return files.keys
    }

    private fun createTestWavFile(): File {
        val testFile = directoryProvider.tempDirectory.resolve("test-take-${Date().time}.wav")
            .apply { createNewFile(); deleteOnExit() }

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
}