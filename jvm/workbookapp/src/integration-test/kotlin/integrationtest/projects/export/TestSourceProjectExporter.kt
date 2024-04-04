/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package integrationtest.projects.export

import integrationtest.createTestWavFile
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.enUlbTestMetadata
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporterFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.SourceProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestSourceProjectExporter {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    @Inject
    lateinit var workbookRepository: IWorkbookRepository
    @Inject
    lateinit var exportSourceProvider: Provider<SourceProjectExporter>
    @Inject
    lateinit var importRcFactoryProvider: Provider<RCImporterFactory>

    private val importer
        get() = importRcFactoryProvider.get().makeImporter()

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    private val db: DatabaseEnvironment
        get() = dbEnvProvider.get()

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
    private lateinit var projectFilesAccessor: ProjectFilesAccessor

    @Before
    fun setUp() {
        db.import("en-x-demo1-ulb-rev.zip")
        workbook = workbookRepository.getProjects().blockingGet()
            .find { it.target.slug == "rev" }!!

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
    fun exportAsSource() {
        prepareTakeForExport()

        val result = exportSourceProvider.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                options = null
            )
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        val exportFile = outputDir.listFiles().first()
        val chapterSourceAudio = getSourceAudio(
            targetMetadata.copy(path = exportFile),
            "rev",
            1
        )
        assertNotNull(chapterSourceAudio)

        ResourceContainer.load(exportFile).use { rc ->
            assertEquals(1, rc.media?.projects?.size ?: 0)

            val files = getExportedFiles(rc)
            assertEquals(2, files.size)
            assertTrue(files.any { it.endsWith(".mp3") })
            assertTrue(files.any { it.endsWith(".cue") })
        }
    }

    @Test
    fun `export source project has no media when no take selected`() {
        val result = exportSourceProvider.get()
            .export(outputDir, workbook, callback = null, options = null)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)
        ResourceContainer.load(outputDir.listFiles().first()).use { rc ->
            val filePaths = getExportedFiles(rc)
            assertEquals(0, filePaths.size)
        }
    }

    @Test
    fun testEstimateExportSize() {
        prepareTakeForExport()

        var expectedSize = 12L
        var computedSize = exportSourceProvider.get()
            .estimateExportSize(workbook, listOf(1))

        Assert.assertEquals("Estimated export size should be $expectedSize bytes", expectedSize, computedSize)

        expectedSize = 0L
        computedSize = exportSourceProvider.get()
            .estimateExportSize(workbook, listOf())

        Assert.assertEquals("Estimated export size should be $expectedSize bytes", expectedSize, computedSize)
    }

//    @Test
//    fun exportSourceProjectThenImport() {
//        // export as source
//        prepareTakeForExport()
//        val result = exportSourceProvider.get()
//            .export(outputDir, workbook, callback = null, options = null)
//            .blockingGet()
//
//        assertEquals(ExportResult.SUCCESS, result)
//
//        // import source from previously exported step
//        val importResult = importer
//            .import(outputDir.listFiles().first())
//            .blockingGet()
//
//        assertEquals(ImportResult.SUCCESS, importResult)
//
//        db.assertRowCounts(
//            RowCount(
//                contents = mapOf(
//                    ContentType.META to 3589,
//                    ContentType.TEXT to 62228
//                ),
//                collections = 3767,
//                links = 0
//            )
//        )
//    }

    @Test
    fun exportSourceAudioWithCompiledChapter() {
        prepareChapterContentReadyToCompile()

        val result = exportSourceProvider.get()
            .export(outputDir, workbook, callback = null, options = null)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        val exportFile = outputDir.listFiles().first()
        val chapterSourceAudio = getSourceAudio(
            targetMetadata.copy(path = exportFile),
            "rev",
            1
        )
        assertNotNull(chapterSourceAudio)

        ResourceContainer.load(exportFile).use { rc ->
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

    private fun prepareTakeForExport() {
        val testTake = createTestWavFile(directoryProvider.tempDirectory)
        val take = Take(
            "chapter-take",
            testTake,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select a take to be included when export
        workbook.target.chapters.blockingFirst().audio.selectTake(take)
        OratureAudioFile(testTake).apply {
            addMarker<VerseMarker>(VerseMarker(1,1, 1))
            update()
        }
    }

    private fun prepareChapterContentReadyToCompile() {
        val takeFile = createTestWavFile(directoryProvider.tempDirectory)
        val take = Take(
            "chapter-take",
            takeFile,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select take for each chunk so that the chapter is ready to compile
        val chapter = workbook.target
            .chapters.blockingFirst()
        chapter
            .chunks
            .timeout(5, TimeUnit.SECONDS)
            .blockingGet()
            ?.forEach {
                it.audio.selectTake(take)
            }

        OratureAudioFile(takeFile).apply {
            addMarker<VerseMarker>(VerseMarker(1, 1, 1))
            update()
        }
    }

    private fun getSourceAudio(
        metadata: ResourceMetadata,
        project: String,
        chapter: Int
    ): SourceAudio? {
        return SourceAudioAccessor(
            directoryProvider,
            metadata,
            project
        ).getChapter(chapter)
    }
}
