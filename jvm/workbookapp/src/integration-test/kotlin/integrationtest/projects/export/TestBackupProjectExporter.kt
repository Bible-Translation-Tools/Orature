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
package integrationtest.projects.export

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.TakeCheckingState
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.FileNamer.Companion.takeFilenamePattern
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.TakeCheckingStatusMap
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
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
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get() // bootstrap the db
    private val takesPerChapter = 2
    private val contributors = listOf("user1", "user2")
    private val verseChecking = TakeCheckingState(CheckingStatus.VERSE, "test-checksum")
    private val seedProject = buildProjectFile()
    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        importer.get().import(seedProject).blockingGet()
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
        val chapterFilter = ExportOptions(chapters = listOf(1, 3))
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                workbook,
                options = chapterFilter,
                callback = null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val chapterToTakes = getTakesByChapterFromProject(file!!)

        Assert.assertEquals(
            chapterFilter.chapters,
            chapterToTakes.keys.toList().sorted()
        )
        Assert.assertEquals(
            takesPerChapter * chapterFilter.chapters.size,
            chapterToTakes.values.sum()
        )
        Assert.assertEquals(
            "Chapters from selected metadata file should match filter.",
            chapterFilter.chapters,
            chaptersFromSelectedTakesFile(file)
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
                Assert.assertEquals(1, takeCheckingMap.values.filter { it == verseChecking }.size )
            }
        }
    }

    private fun buildProjectFile(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setContributors(contributors)
            .addTake(1, ContentType.META, 1, true)
            .addTake(2, ContentType.META, 1, true)
            .addTake(3, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.TEXT, 1, true, chapter = 2, start = 1, end = 1)
            .addTake(3, ContentType.TEXT, 1, true, chapter = 3, start = 1, end = 1, checking = verseChecking)
            .buildFile()
    }

    private fun getTakesByChapterFromProject(file: File): Map<Int, Int> {
        val chapterToTakeCount = mutableMapOf<Int, Int>()

        val parseChapter: (String) -> Int = { name: String ->
            takeFilenamePattern
                .matcher(name)
                .apply { find() }
                .group(1)
                .toInt()
        }

        ResourceContainer.load(file).use { rc ->
            val extensionFilter = AudioFileFormat.values().map { it.extension }
            val fileStreamMap = rc.accessor.getInputStreams(".", extensionFilter)
            try {
                fileStreamMap.keys.forEach { name ->
                    val chapterNumber = parseChapter(name)
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
            if (it.accessor.fileExists(".apps/orature/selected.txt"))
                {
                    it.accessor.getReader(".apps/orature/selected.txt").use {
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
}
