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
package integrationtest.projects.importer

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.enUlbTestMetadata
import integrationtest.projects.DatabaseEnvironment
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.exporter.AudioProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestAudioProjectExporter {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject
    lateinit var importer: Provider<ImportProjectUseCase>

    @Inject
    lateinit var exportAudioUseCase: Provider<AudioProjectExporter>

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get() // bootstrap the db
    private val contributors = listOf("user1", "user2")
    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        importer.get().import(setUpProject()).blockingGet()
        workbook = workbookRepository.getProjects().blockingGet()
            .find { it.target.slug == ResourceContainerBuilder.defaultProjectSlug }!!
        outputDir = createTempDirectory("orature-export-test").toFile()
    }

    @After
    fun cleanUp() {
        outputDir.deleteRecursively()
    }

    @Test
    fun testExportAudio() {
        exportAudioUseCase.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                options = null
            )
            .blockingGet()

        val takes = outputDir.walk().filter { AudioFileFormat.isSupported(it.extension) }.toList()
        Assert.assertEquals(2, takes.size)

        val takeContributors = OratureAudioFile(takes.first()).metadata.artists()
        Assert.assertEquals(contributors, takeContributors)
    }

    @Test
    fun testExportAudioWithFilter() {
        exportAudioUseCase.get()
            .export(
                outputDir,
                workbook,
                callback = null,
                ExportOptions(chapters = listOf(2))
            )
            .blockingGet()

        val takes = outputDir.walk().filter { AudioFileFormat.isSupported(it.extension) }.toList()
        Assert.assertEquals(
            "Export directory must only contain the chapter(s) specified in Options. Found: $takes",
            1,
            takes.size
        )
    }

    private fun setUpProject(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setOngoingProject(true)
            .setContributors(contributors)
            .addTake(1, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.META, 1, true)
            .buildFile()
    }

}