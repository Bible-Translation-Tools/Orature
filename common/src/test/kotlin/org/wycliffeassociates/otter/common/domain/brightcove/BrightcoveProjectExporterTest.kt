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
package org.wycliffeassociates.otter.common.domain.brightcove

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.AssociatedTranslation
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeHolder
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.AudioExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.BrightcoveProjectExporter
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.time.LocalDate
import kotlin.io.path.createTempDirectory

class BrightcoveProjectExporterTest {

    private lateinit var tempDir: File
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        tempDir = createTempDirectory("brightcove-export-temp").toFile()
        outputDir = createTempDirectory("brightcove-export-out").toFile()
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
        outputDir.deleteRecursively()
    }

    @Test
    fun exportSelectedTakesOnly() {
        val directoryProvider = mockDirectoryProvider(tempDir)
        val configProvider = mockk<BrightcoveConfigProvider> {
            every { load() } returns Single.just(
                BrightcoveConfig("acct", "client", "secret")
            )
        }
        val brightcoveClient = mockk<BrightcoveClient> {
            every { createVideo(any(), any()) } returnsMany listOf(
                Single.just("v1"),
                Single.just("v2")
            )
            every { uploadSource(any(), any(), any()) } returnsMany listOf(
                Single.just(BrightcoveUploadResult("m1")),
                Single.just(BrightcoveUploadResult("m2"))
            )
            every { ingest(any(), any(), any()) } returnsMany listOf(
                Single.just(BrightcoveIngestResult("j1")),
                Single.just(BrightcoveIngestResult("j2"))
            )
        }
        val countryResolver = mockk<CountryInfoResolver> {
            every { resolve(any()) } returns Single.just(CountryInfo(null, null))
        }
        val audioExporter = mockk<AudioExporter> {
            every { exportMp3(any(), any(), any()) } returns Completable.complete()
        }

        val exporter = BrightcoveProjectExporter(
            directoryProvider,
            configProvider,
            brightcoveClient,
            countryResolver
        ).apply {
            this.audioExporter = audioExporter
        }

        val workbook = buildWorkbook(directoryProvider, mapOf(1 to true, 2 to true, 3 to true))
        val result = exporter.export(
            outputDir,
            workbook,
            callback = null,
            options = ExportOptions(listOf(1, 2))
        ).blockingGet()

        assertEquals(ExportResult.SUCCESS, result)
        verify(exactly = 2) { audioExporter.exportMp3(any(), any(), any()) }
        verify(exactly = 2) { brightcoveClient.createVideo(any(), any()) }
        verify(exactly = 2) { brightcoveClient.uploadSource(any(), any(), any()) }
        verify(exactly = 2) { brightcoveClient.ingest(any(), any(), any()) }

        val reportFile = outputDir.listFiles()?.firstOrNull { it.name.startsWith("brightcove-export-") }
        assertNotNull(reportFile)

        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        val report = mapper.readValue<BrightcoveExportReport>(reportFile!!)
        assertEquals(2, report.entries.size)
        assertTrue(report.entries.all { it.status == "SUCCESS" })
    }

    @Test
    fun continuesOnFailureAndReturnsFailure() {
        val directoryProvider = mockDirectoryProvider(tempDir)
        val configProvider = mockk<BrightcoveConfigProvider> {
            every { load() } returns Single.just(
                BrightcoveConfig("acct", "client", "secret")
            )
        }
        val brightcoveClient = mockk<BrightcoveClient> {
            every { createVideo(any(), any()) } returnsMany listOf(
                Single.just("v1"),
                Single.just("v2")
            )
            every { uploadSource(any(), any(), any()) } returnsMany listOf(
                Single.just(BrightcoveUploadResult("m1")),
                Single.just(BrightcoveUploadResult("m2"))
            )
            every { ingest(any(), any(), any()) } returnsMany listOf(
                Single.error(RuntimeException("fail")),
                Single.just(BrightcoveIngestResult("j2"))
            )
        }
        val countryResolver = mockk<CountryInfoResolver> {
            every { resolve(any()) } returns Single.just(CountryInfo(null, null))
        }
        val audioExporter = mockk<AudioExporter> {
            every { exportMp3(any(), any(), any()) } returns Completable.complete()
        }

        val exporter = BrightcoveProjectExporter(
            directoryProvider,
            configProvider,
            brightcoveClient,
            countryResolver
        ).apply {
            this.audioExporter = audioExporter
        }

        val workbook = buildWorkbook(directoryProvider, mapOf(1 to true, 2 to true))
        val result = exporter.export(
            outputDir,
            workbook,
            callback = null,
            options = ExportOptions(listOf(1, 2))
        ).blockingGet()

        assertEquals(ExportResult.FAILURE, result)
        verify(exactly = 2) { brightcoveClient.createVideo(any(), any()) }

        val reportFile = outputDir.listFiles()?.firstOrNull { it.name.startsWith("brightcove-export-") }
        assertNotNull(reportFile)

        val mapper = ObjectMapper(JsonFactory()).registerKotlinModule()
        val report = mapper.readValue<BrightcoveExportReport>(reportFile!!)
        assertEquals(2, report.entries.size)
        assertTrue(report.entries.any { it.status == "FAILURE" })
    }

    private fun buildWorkbook(
        directoryProvider: IDirectoryProvider,
        chapterHasSelectedTake: Map<Int, Boolean>
    ): Workbook {
        val language = Language("bzs", "Bzs", "", "", false, "")
        val metadata = ResourceMetadata(
            conformsTo = "",
            creator = "creator",
            description = "",
            format = "",
            identifier = "ulb",
            issued = LocalDate.now(),
            language = language,
            modified = LocalDate.now(),
            publisher = "",
            subject = "",
            type = ContainerType.Book,
            title = "",
            version = "1",
            license = "CC BY-SA 4.0",
            path = tempDir
        )

        val chapters = chapterHasSelectedTake.keys.sorted().map { chapterNumber ->
            buildChapter(chapterNumber, chapterHasSelectedTake.getValue(chapterNumber))
        }

        val book = Book(
            collectionId = 1,
            sort = 41,
            slug = "mat",
            title = "Mateus",
            label = "MAT",
            chapters = io.reactivex.Observable.fromIterable(chapters),
            resourceMetadata = metadata,
            linkedResources = listOf(),
            modifiedTs = null,
            subtreeResources = listOf()
        )

        return Workbook(
            directoryProvider,
            source = book,
            target = book,
            translation = AssociatedTranslation(
                BehaviorRelay.createDefault(1.0),
                BehaviorRelay.createDefault(1.0)
            )
        )
    }

    private fun buildChapter(chapterNumber: Int, selectedTake: Boolean): Chapter {
        val take = if (selectedTake) {
            Take(
                name = "take$chapterNumber",
                file = tempDir.resolve("take$chapterNumber.wav"),
                number = chapterNumber,
                format = MimeType.WAV,
                createdTimestamp = LocalDate.now()
            )
        } else {
            null
        }

        val audio = AssociatedAudio(
            takes = ReplayRelay.create(),
            selected = BehaviorRelay.createDefault(TakeHolder(take))
        )

        return Chapter(
            sort = chapterNumber,
            title = chapterNumber.toString(),
            label = chapterNumber.toString(),
            audio = audio,
            resources = listOf(),
            subtreeResources = listOf(),
            lazychunks = lazy { BehaviorRelay.createDefault(listOf()) },
            chunkCount = Single.just(0),
            addChunk = { Completable.complete() },
            reset = { Completable.complete() }
        )
    }

    private fun mockDirectoryProvider(baseDir: File): IDirectoryProvider {
        val provider = mockk<IDirectoryProvider>(relaxed = true)
        val projectDir = baseDir.resolve("project").apply { mkdirs() }
        val sourceDir = baseDir.resolve("source").apply { mkdirs() }
        val sourceAudioDir = baseDir.resolve("source-audio").apply { mkdirs() }
        val audioDir = baseDir.resolve("audio").apply { mkdirs() }
        val temp = baseDir.resolve("temp").apply { mkdirs() }

        every { provider.getProjectDirectory(any(), any(), any<org.wycliffeassociates.otter.common.data.primitives.Collection>()) } returns projectDir
        every { provider.getProjectSourceDirectory(any(), any(), any<org.wycliffeassociates.otter.common.data.primitives.Collection>()) } returns sourceDir
        every { provider.getProjectSourceAudioDirectory(any(), any(), any<String>()) } returns sourceAudioDir
        every { provider.getProjectAudioDirectory(any(), any(), any<org.wycliffeassociates.otter.common.data.primitives.Collection>()) } returns audioDir
        every { provider.tempDirectory } returns temp

        return provider
    }
}
