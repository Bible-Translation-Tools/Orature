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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import integrationtest.createTestWavFile
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import io.mockk.every
import io.mockk.spyk
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.Chunkification
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.project.InProgressNarrationFileFormat
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.content.CreateChunks
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.inProgressNarrationPattern
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.takeFilenamePattern
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.TakeCheckingStatusMap
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestBackupProjectExporter {

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject
    lateinit var importer: Provider<ImportProjectUseCase>

    @Inject
    lateinit var exportBackupUseCase: Provider<BackupProjectExporter>

    @Inject
    lateinit var createChunks: CreateChunks

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get() // bootstrap the db
    private val takesPerChapter = 2
    private val narrationChapter = 7
    private val contributors = listOf("user1", "user2")
    private val verseChecking = TakeCheckingState(CheckingStatus.VERSE, "test-checksum")
    private val seedProject = buildProjectFile()
    private val narrationBackup = buildNarrationBackup()
    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        importer.get().import(seedProject).blockingGet()
        importer.get().import(narrationBackup).blockingGet()
        workbook = workbookRepository.getProjects().blockingGet()
            .find { it.target.slug == ResourceContainerBuilder.defaultProjectSlug }!!
        outputDir = createTempDirectory("orature-export-test").toFile()
    }

    @After
    fun cleanUp() {
        outputDir.deleteRecursively()
    }

    @Test
    fun exportProjectWithContributorInfo() {
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                options = null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val exportedContributorList = ResourceContainer.load(file!!).use {
            it.manifest.dublinCore.contributor.toList()
        }
        Assert.assertEquals(contributors, exportedContributorList)
    }

    @Test
    fun exportProjectWithChapterFilter() {
        val chaptersToExport = listOf(1, 3)
        val stubbedWorkbook = prepareChunksInChapter(chaptersToExport)
        val chapterFilter = ExportOptions(chaptersToExport)
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                stubbedWorkbook,
                options = chapterFilter,
                callback = null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val chapterTakeMap = getTakesByChapterFromProject(file!!)
        val totalTakes = takesPerChapter * chaptersToExport.size

        Assert.assertEquals(
            chaptersToExport,
            chapterTakeMap.keys.toList().sorted()
        )
        Assert.assertEquals(
            totalTakes,
            chapterTakeMap.values.sum()
        )
        Assert.assertEquals(
            "Chapters from selected metadata file should match filter.",
            chaptersToExport,
            chaptersFromSelectedTakesFile(file)
        )
        Assert.assertEquals(
            "Chapters from chunk file should match filter.",
            chaptersToExport,
            chaptersInChunksFile(file)
        )
    }

    @Test
    fun exportTranslationWithChecking() {
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                options = null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        ResourceContainer.load(file!!).use { rc ->
            rc.accessor.getInputStream(ResourceContainerBuilder.checkingStatusFilePath).use { stream ->
                val mapper = ObjectMapper(JsonFactory())
                    .registerKotlinModule()
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                val takeCheckingMap = mapper.readValue<TakeCheckingStatusMap>(stream)

                Assert.assertEquals(6, takeCheckingMap.size)
                Assert.assertEquals(1, takeCheckingMap.values.filter { it == verseChecking }.size)
            }
        }
    }

    /**
     * Create chunks based on stubbed source audio
     */
    private fun prepareChunksInChapter(chaptersToExport: List<Int>): Workbook {
        val cues = listOf(
            AudioCue(0, "1"),
            AudioCue(5000, "2")
        )
        val sourceAudioFile = createTestWavFile(directoryProvider.tempDirectory)
        OratureAudioFile(sourceAudioFile).apply {
            addMarker<VerseMarker>(VerseMarker(1, 1, 0))
            addMarker<VerseMarker>(VerseMarker(2, 2, 5_000))
            addMarker<VerseMarker>(VerseMarker(3, 3, 10_000))
            update()
        }
        val sourceAudio = SourceAudio(sourceAudioFile, 0, 100_000)
        val sourceAudioAccessor = spyk(
            SourceAudioAccessor(
                directoryProvider,
                workbook.source.resourceMetadata,
                workbook.source.slug
            )
        )
        val workbookSpy = spyk(workbook)

        every { sourceAudioAccessor.getChapter(any<Int>(), any()) } returns sourceAudio
        every { workbookSpy.sourceAudioAccessor } returns sourceAudioAccessor
        every { workbookSpy.target } answers { callOriginal() }
        every { workbookSpy.source } answers { callOriginal() }

        val chapters = workbook.target.chapters.filter { it.sort in chaptersToExport }.toList().blockingGet()
        chapters.forEach {
            createChunks.createUserDefinedChunks(workbookSpy, it, cues).blockingAwait()
        }

        return workbookSpy
    }

    @Test
    fun exportNarrationBackup() {
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                options = null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val chapterToNarrationFiles = mutableMapOf<Int, MutableList<String>>()

        ResourceContainer.load(file!!).use { rc ->
            val extensionFilter = InProgressNarrationFileFormat.values().map { it.extension }
            val fileStreamMap = rc.accessor.getInputStreams(".", extensionFilter)
            try {
                fileStreamMap.keys.forEach { name ->
                    val chapterNumber = parseChapter(name, inProgressNarrationPattern)
                    if (chapterNumber !in chapterToNarrationFiles) {
                        chapterToNarrationFiles[chapterNumber] = mutableListOf()
                    }
                    chapterToNarrationFiles[chapterNumber]?.add(name)
                }
            } catch (_: Exception) {
            } finally {
                fileStreamMap.values.forEach { it.close() }
            }
        }

        Assert.assertEquals(true, narrationChapter in chapterToNarrationFiles)
        Assert.assertEquals(2, chapterToNarrationFiles[narrationChapter]?.size)
    }

    @Test
    fun testEstimateExportSize() {
        var expectedSize = 192L
        var computedSize = exportBackupUseCase.get()
            .estimateExportSize(workbook, listOf(1, 2))

        Assert.assertEquals("Estimated backup size should be $expectedSize bytes", expectedSize, computedSize)

        expectedSize = 288L
        computedSize = exportBackupUseCase.get()
            .estimateExportSize(workbook, listOf(1, 2, 3))

        Assert.assertEquals("Estimated backup size should be $expectedSize bytes", expectedSize, computedSize)

        expectedSize = 0L
        computedSize = exportBackupUseCase.get()
            .estimateExportSize(workbook, listOf())

        Assert.assertEquals("Estimated backup size should be $expectedSize bytes", expectedSize, computedSize)
    }

    private fun parseChapter(path: String, pattern: Pattern): Int {
        return pattern
            .matcher(path)
            .apply { find() }
            .group(1)
            .toInt()
    }

    private fun buildProjectFile(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setOngoingProject(true)
            .setContributors(contributors)
            .addTake(1, ContentType.META, 1, true)
            .addTake(2, ContentType.META, 1, true)
            .addTake(3, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.TEXT, 1, true, chapter = 2, start = 1, end = 1)
            // set checking status to be imported
            .addTake(3, ContentType.TEXT, 1, true, chapter = 3, start = 1, end = 1, checking = verseChecking)
            .buildFile()
    }

    private fun buildNarrationBackup(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setOngoingProject(true)
            .setContributors(contributors)
            .addInProgressNarration(narrationChapter)
            .buildFile()
    }

    private fun getTakesByChapterFromProject(file: File): Map<Int, Int> {
        val chapterToTakeCount = mutableMapOf<Int, Int>()

        ResourceContainer.load(file).use { rc ->
            val extensionFilter = AudioFileFormat.values().map { it.extension }
            val fileStreamMap = rc.accessor.getInputStreams(".", extensionFilter)
            try {
                fileStreamMap.keys.forEach { name ->
                    val chapterNumber = parseChapter(name, takeFilenamePattern)
                    chapterToTakeCount[chapterNumber] = 1 + chapterToTakeCount.getOrPut(chapterNumber) { 0 }
                }
            } finally {
                fileStreamMap.values.forEach { it.close() }
            }
        }

        return chapterToTakeCount
    }

    private fun chaptersFromSelectedTakesFile(projectFile: File): List<Int> {
        var lines = mutableListOf<String>()
        ResourceContainer.load(projectFile).use {
            if (it.accessor.fileExists(ResourceContainerBuilder.selectedTakesFilePath)) {
                it.accessor.getReader(ResourceContainerBuilder.selectedTakesFilePath).use {
                    lines.addAll(it.readText().split("\n"))
                }
            }
        }
        return lines
            .filter { it.isNotBlank() }
            .map {
                takeFilenamePattern
                    .matcher(it)
                    .apply { find() }
                    .group(1)
                    .toInt()
            }
            .distinct()
    }

    private fun chaptersInChunksFile(projectFile: File): List<Int> {
        ResourceContainer.load(projectFile).use {
            return if (it.accessor.fileExists(ResourceContainerBuilder.chunksFilePath)) {
                try {
                    it.accessor.getInputStream(ResourceContainerBuilder.chunksFilePath).use { stream ->
                        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
                        val chunks: Chunkification = mapper.readValue(stream)
                        chunks.keys.toList()
                    }
                } catch (e: MismatchedInputException) {
                    listOf<Int>()
                }
            } else {
                listOf()
            }
        }
    }
}
