/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Observable
import javafx.application.Platform
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.testfx.api.FxToolkit
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
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Semaphore

class ChapterPageViewModelTest {

    private val testApp: TestApp = TestApp()
    private lateinit var chapterPageViewModel: ChapterPageViewModel
    private lateinit var workbookDataStore: WorkbookDataStore

    private val chunk1 = Chunk(
        sort = 1,
        audio = createAssociatedAudio(),
        textItem = TextItem("Chunk 1", MimeType.USFM),
        start = 1,
        end = 2,
        contentType = ContentType.TEXT,
        resources = listOf(),
        label = "Chunk"
    )

    private val chunk2 = Chunk(
        sort = 2,
        audio = createAssociatedAudio(),
        textItem = TextItem("Chunk 2", MimeType.USFM),
        start = 3,
        end = 4,
        contentType = ContentType.TEXT,
        resources = listOf(),
        label = "Chunk"
    )

    private val chapter1 = Chapter(
        1,
        "1",
        "1",
        createAssociatedAudio(),
        listOf(),
        listOf(),
        Observable.fromIterable(listOf(chunk1, chunk2))
    )

    private val chapter2 = Chapter(
        2,
        "2",
        "2",
        createAssociatedAudio(),
        listOf(),
        listOf(),
        Observable.fromIterable(listOf(chunk1, chunk2))
    )

    private val english = Language(
        "en",
        "English",
        "English",
        "ltr",
        true,
        "Europe"
    )

    private val takeFile = File(javaClass.getResource("/files/test.wav")!!.file)
    private val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.MIN)

    private val resourceMetadata = mock<ResourceMetadata>()

    private val book = mock<Book> {
        on { resourceMetadata } doReturn resourceMetadata
        on { chapters } doReturn Observable.fromIterable(listOf(chapter1, chapter2))
        on { language } doReturn english
    }

    private val sourceAudioAccessor = mock<SourceAudioAccessor> {
        on { getChapter(any()) } doReturn SourceAudio(takeFile, 0, 1)
    }

    private val workbook = mock<Workbook> {
        on { source } doReturn book
        on { target } doReturn book
        on { sourceAudioAccessor } doReturn sourceAudioAccessor
    }

    private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())

    private fun waitForRunLater() {
        val semaphore = Semaphore(0)
        Platform.runLater { semaphore.release() }
        semaphore.acquire()
    }

    @Before
    fun setup() {
        FxToolkit.registerPrimaryStage()
        FxToolkit.setupApplication { testApp }

        val configureAudio = ConfigureAudioSystem(
            testApp.dependencyGraph.injectConnectionFactory(),
            testApp.dependencyGraph.injectAudioDeviceProvider(),
            testApp.dependencyGraph.injectAppPreferencesRepository()
        )
        configureAudio.configure()

        workbookDataStore = find()
        workbookDataStore.activeWorkbookProperty.set(workbook)
        workbookDataStore.activeChapterProperty.set(chapter1)

        chapterPageViewModel = find()
    }

    @Test
    fun onCardSelection_chapterCard() {
        val chapterCard = CardData(chapter2)
        chapterPageViewModel.onCardSelection(chapterCard)

        Assert.assertEquals(
            chapterCard.chapterSource,
            chapterPageViewModel.workbookDataStore.activeChapterProperty.value
        )

        Assert.assertNull(chapterPageViewModel.workbookDataStore.activeChunkProperty.value)
    }

    @Test
    fun onCardSelection_chunkCard() {
        val chunkCard = CardData(chunk1)
        chapterPageViewModel.onCardSelection(chunkCard)

        Assert.assertEquals(
            chunkCard.chunkSource,
            chapterPageViewModel.workbookDataStore.activeChunkProperty.value
        )
    }

    @Test
    fun checkCanCompile_someSelected() {
        chunk1.audio.selectTake(take)
        chunk2.audio.selectTake(null)

        waitForRunLater()

        chapterPageViewModel.checkCanCompile()

        Assert.assertEquals(false, chapterPageViewModel.canCompileProperty.value)
    }

    @Test
    fun checkCanCompile_allSelected() {
        chunk1.audio.selectTake(take)
        chunk2.audio.selectTake(take)

        waitForRunLater()

        chapterPageViewModel.checkCanCompile()

        Assert.assertTrue(chapterPageViewModel.canCompileProperty.value == true)
    }

    @Test
    fun setWorkChunk_initially() {
        val chunkCard = CardData(chunk1)
        chapterPageViewModel.setWorkChunk()

        Assert.assertTrue(chapterPageViewModel.noTakesProperty.value == true)
        Assert.assertEquals(chunkCard.sort, chapterPageViewModel.workChunkProperty.value.sort)
    }

    @Test
    fun setWorkChunk_secondChunk() {
        val chunkCard = CardData(chunk2)
        chunk1.audio.insertTake(take)
        chunk1.audio.selectTake(take)

        waitForRunLater()

        chapterPageViewModel.setWorkChunk()

        Assert.assertTrue(chapterPageViewModel.noTakesProperty.value == false)
        Assert.assertEquals(chunkCard, chapterPageViewModel.workChunkProperty.value)
    }

    @Test
    fun setSelectedChapterTake_takeSelected() {
        chapter1.audio.selectTake(take)

        waitForRunLater()

        Assert.assertEquals(take, chapterPageViewModel.selectedChapterTakeProperty.value)
    }

    @Test
    fun recordChapter_recordsTake1() {
        chapterPageViewModel.recordChapter()

        Assert.assertEquals(PluginType.RECORDER, chapterPageViewModel.contextProperty.value)
        Assert.assertEquals(1, workbookDataStore.activeTakeNumberProperty.value)
    }
}
