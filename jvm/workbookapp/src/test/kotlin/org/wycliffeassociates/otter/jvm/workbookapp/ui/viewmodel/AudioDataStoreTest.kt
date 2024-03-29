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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.BehaviorRelay
import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.utils.writeWavFile
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

class AudioDataStoreTest {
    companion object {
        private val testApp: TestApp = TestApp()

        private lateinit var audioDataStore: AudioDataStore
        private lateinit var workbookDataStore: WorkbookDataStore

        private val directoryProvider = testApp.dependencyGraph.injectDirectoryProvider()
        private val tempDir = directoryProvider.tempDirectory
        private var takeFile = File(tempDir, "take1.wav")
        private var sourceTakeFile = File(tempDir, "sourceTake.wav")

        private var sourceChunk = createChunk()
        private var targetChunk = createChunk()
        private var sourceChapter = createChapter(sourceChunk)
        private var targetChapter = createChapter(targetChunk)

        private val english = Language(
            "en",
            "English",
            "English",
            "ltr",
            true,
            "Europe"
        )

        private val resourceMetadata = mock<ResourceMetadata> {
            on { identifier } doReturn "ulb"
        }

        private val collection = mock<Collection> {
            on { slug } doReturn "gen"
            on { titleKey } doReturn "gen"
        }

        private val sourceBook = mock<Book> {
            on { resourceMetadata } doReturn resourceMetadata
            on { chapters } doReturn Observable.fromIterable(listOf(sourceChapter))
            on { language } doReturn english
            on { slug } doReturn "gen"
            on { label } doReturn "Genesis"
            on { title } doReturn "Genesis"
            on { modifiedTs } doReturn LocalDateTime.now()
            on { toCollection() } doReturn collection
        }

        private val targetBook = mock<Book> {
            on { resourceMetadata } doReturn resourceMetadata
            on { chapters } doReturn Observable.fromIterable(listOf(targetChapter))
            on { language } doReturn english
            on { slug } doReturn "gen"
            on { label } doReturn "Genesis"
            on { title } doReturn "Genesis"
            on { modifiedTs } doReturn LocalDateTime.now()
            on { toCollection() } doReturn collection
        }

        private val sourceAudioAccessor = mock<SourceAudioAccessor> {
            on { getChapter(any(), any()) } doReturn SourceAudio(sourceTakeFile, 0, 10)
            on { getChunk(any(), any(), any(), any()) } doReturn SourceAudio(sourceTakeFile, 0, 1)
        }

        private val workbook = mock<Workbook> {
            on { source } doReturn sourceBook
            on { target } doReturn targetBook
            on { sourceAudioAccessor } doReturn sourceAudioAccessor
        }

        private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())

        private fun createChunk(): Chunk {
            return Chunk(
                sort = 1,
                audio = createAssociatedAudio(),
                textItem = TextItem("Chunk 1", MimeType.USFM),
                start = 1,
                end = 1,
                contentType = ContentType.TEXT,
                resources = listOf(),
                label = "chunk",
                draftNumber = 1
            )
        }

        private fun createChapter(chunk: Chunk): Chapter {
            val chunks = BehaviorRelay.create<List<Chunk>>()
            chunks.accept(listOf(chunk))
            return Chapter(
                1,
                "1",
                "1",
                createAssociatedAudio(),
                listOf(),
                listOf(),
                lazy { chunks },
                Single.just(1),
                { Completable.complete() },
                { Completable.complete() }
            )
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            writeWavFile(sourceTakeFile)

            val configureAudio = ConfigureAudioSystem(
                testApp.dependencyGraph.injectConnectionFactory(),
                testApp.dependencyGraph.injectAudioDeviceProvider(),
                testApp.dependencyGraph.injectAppPreferencesRepository()
            )
            configureAudio.configure()

            audioDataStore = find()
            workbookDataStore = find()
            workbookDataStore.activeWorkbookProperty.set(workbook)
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            FxToolkit.hideStage()
            FxToolkit.cleanupStages()
            FxToolkit.cleanupApplication(testApp)
        }
    }

    @Before
    fun prepare() {
        writeWavFile(takeFile)
    }

    @After
    fun cleanup() {
        workbookDataStore.activeChapterProperty.set(null)
        workbookDataStore.activeChunkProperty.set(null)
    }

    @Test
    fun `no source audio when there is no active chapter`() {
        audioDataStore.updateSourceAudio()

        Assert.assertNull(audioDataStore.sourceAudioProperty.value)
    }

    @Test
    fun `there is chapter source audio for active chapter`() {
        workbookDataStore.activeChapterProperty.set(targetChapter)
        audioDataStore.updateSourceAudio()

        Assert.assertEquals(sourceTakeFile, audioDataStore.sourceAudioProperty.value?.file)
        Assert.assertEquals(0, audioDataStore.sourceAudioProperty.value?.start)
        Assert.assertEquals(10, audioDataStore.sourceAudioProperty.value?.end)
    }

    @Test
    fun `there is chunk source audio for active chunk`() {
        workbookDataStore.activeChapterProperty.set(targetChapter)
        workbookDataStore.activeChunkProperty.set(targetChunk)
        audioDataStore.updateSourceAudio()

        Assert.assertEquals(sourceTakeFile, audioDataStore.sourceAudioProperty.value?.file)
        Assert.assertEquals(0, audioDataStore.sourceAudioProperty.value?.start)
        Assert.assertEquals(1, audioDataStore.sourceAudioProperty.value?.end)
    }

    @Test
    fun `no chapter player when there is no active chapter`() {
        audioDataStore.updateSelectedChapterPlayer()

        Assert.assertNull(audioDataStore.selectedChapterPlayerProperty.value)
        Assert.assertNull(audioDataStore.targetAudioProperty.value)
    }

    @Test
    fun `no chapter player when there is no selected take`() {
        workbookDataStore.activeChapterProperty.set(targetChapter)
        audioDataStore.updateSelectedChapterPlayer()

        Assert.assertNull(audioDataStore.selectedChapterPlayerProperty.value)
        Assert.assertNull(audioDataStore.targetAudioProperty.value)
    }

    @Test
    fun `there are chapter and target audio players for active chapter with selected take`() {
        val take1 = Take("take1", takeFile, 1, MimeType.USFM, LocalDate.now())
        targetChapter.audio.insertTake(take1)
        targetChapter.audio.selectTake(take1)

        workbookDataStore.activeChapterProperty.set(targetChapter)
        audioDataStore.updateSelectedChapterPlayer()

        Assert.assertNotNull(audioDataStore.selectedChapterPlayerProperty.value)
        Assert.assertNotNull(audioDataStore.targetAudioProperty.value)
    }

    @Test
    fun `no chapter player for chunk page`() {
        val take1 = Take("take1", takeFile, 1, MimeType.USFM, LocalDate.now())
        targetChapter.audio.insertTake(take1)
        targetChapter.audio.selectTake(take1)

        workbookDataStore.activeChapterProperty.set(targetChapter)
        workbookDataStore.activeChunkProperty.set(targetChunk)
        audioDataStore.updateSelectedChapterPlayer()

        Assert.assertNull(audioDataStore.selectedChapterPlayerProperty.value)
        Assert.assertNull(audioDataStore.targetAudioProperty.value)
    }

    @Test(expected = IllegalStateException::class)
    fun `getting source audio without active chapter throws exception`() {
        audioDataStore.getSourceAudio()
    }

    @Test
    fun `getting source audio for active chapter`() {
        workbookDataStore.activeChapterProperty.set(targetChapter)
        val sourceAudio = audioDataStore.getSourceAudio()

        Assert.assertEquals(sourceTakeFile, sourceAudio?.file)
        Assert.assertEquals(0, sourceAudio?.start)
        Assert.assertEquals(10, sourceAudio?.end)
    }

    @Test
    fun `getting source audio for active chunk`() {
        workbookDataStore.activeChapterProperty.set(targetChapter)
        workbookDataStore.activeChunkProperty.set(targetChunk)
        val sourceAudio = audioDataStore.getSourceAudio()

        Assert.assertEquals(sourceTakeFile, sourceAudio?.file)
        Assert.assertEquals(0, sourceAudio?.start)
        Assert.assertEquals(1, sourceAudio?.end)
    }
}
