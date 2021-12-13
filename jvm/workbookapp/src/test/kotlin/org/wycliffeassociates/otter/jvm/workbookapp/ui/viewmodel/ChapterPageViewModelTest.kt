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
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
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
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.concurrent.CountDownLatch

class ChapterPageViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()

        private lateinit var chapterPageViewModel: ChapterPageViewModel
        private lateinit var audioPluginViewModel: AudioPluginViewModel
        private lateinit var settingsViewModel: SettingsViewModel
        private lateinit var workbookDataStore: WorkbookDataStore
        private val directoryProvider = testApp.dependencyGraph.injectDirectoryProvider()

        private var canCompileListener: ChangeListener<Boolean>? = null
        private var isCompilingListener: ChangeListener<Boolean>? = null
        private var contextListener: ChangeListener<PluginType>? = null
        private var activeChapterListener: ChangeListener<Chapter>? = null
        private var activeChunkListener: ChangeListener<Chunk>? = null
        private var noTakesListener: ChangeListener<Boolean>? = null
        private var activeTakeNumberListener: ChangeListener<Number>? = null
        private var workChunkListener: ChangeListener<CardData>? = null
        private var selectedChapterTakeListener: ChangeListener<Take>? = null
        private var showExportProgressListener: ChangeListener<Boolean>? = null

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

        private val takeFile = File(ChapterPageViewModelTest::class.java.getResource("/files/test.wav")!!.file)

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

        private val resourceMetadata = mock<ResourceMetadata> {
            on { identifier } doReturn "ulb"
        }

        private val book = mock<Book> {
            on { resourceMetadata } doReturn resourceMetadata
            on { chapters } doReturn Observable.fromIterable(listOf(chapter1, chapter2))
            on { language } doReturn english
            on { slug } doReturn "gen"
        }

        private val sourceAudioAccessor = mock<SourceAudioAccessor> {
            on { getChapter(any()) } doReturn SourceAudio(takeFile, 0, 1)
        }

        private val workbook = mock<Workbook> {
            on { source } doReturn book
            on { target } doReturn book
            on { sourceAudioAccessor } doReturn sourceAudioAccessor
        }

        private val recorderPlugin = AudioPluginData(
            1,
            "Recorder",
            "1",
            false,
            true,
            false,
            "",
            listOf(),
            null
        )

        private val projectFilesAccessor = mock<ProjectFilesAccessor> {
            on { audioDir } doReturn File("test")
        }

        private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())

        @BeforeClass
        @JvmStatic fun setup() {
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
            workbookDataStore.activeProjectFilesAccessorProperty.set(projectFilesAccessor)
            workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)

            audioPluginViewModel = find()
            chapterPageViewModel = find()
            settingsViewModel = find()
        }
    }

    @Before
    fun prepare() {
        chapterPageViewModel.selectedChapterTakeProperty.set(null)
        chapterPageViewModel.contextProperty.set(PluginType.RECORDER)
        chapterPageViewModel.noTakesProperty.set(false)
        chapterPageViewModel.workChunkProperty.set(null)
        chapterPageViewModel.canCompileProperty.set(false)

        workbookDataStore.activeTakeNumberProperty.set(0)

        chunk1.audio.selectTake(null)
        chunk1.audio.getAllTakes().map {
            it.deletedTimestamp.accept(DateHolder.now())
        }
        chunk2.audio.selectTake(null)
        chunk2.audio.getAllTakes().map {
            it.deletedTimestamp.accept(DateHolder.now())
        }
    }

    @After
    fun cleanup() {
        canCompileListener?.let {
            chapterPageViewModel.canCompileProperty.removeListener(it)
        }
        contextListener?.let {
            chapterPageViewModel.contextProperty.removeListener(it)
        }
        activeChapterListener?.let {
            workbookDataStore.activeChapterProperty.removeListener(it)
        }
        activeChunkListener?.let {
            workbookDataStore.activeChunkProperty.removeListener(it)
        }
        noTakesListener?.let {
            chapterPageViewModel.noTakesProperty.removeListener(it)
        }
        activeTakeNumberListener?.let {
            workbookDataStore.activeTakeNumberProperty.removeListener(it)
        }
        workChunkListener?.let {
            chapterPageViewModel.workChunkProperty.removeListener(it)
        }
        selectedChapterTakeListener?.let {
            chapterPageViewModel.selectedChapterTakeProperty.removeListener(it)
        }
        isCompilingListener?.let {
            chapterPageViewModel.isCompilingProperty.removeListener(it)
        }
        showExportProgressListener?.let {
            chapterPageViewModel.showExportProgressDialogProperty.removeListener(it)
        }
    }

    private fun <T> createChangeListener(callback: (T) -> Unit): ChangeListener<T> {
        return ChangeListener { _, _, value ->
            callback(value)
        }
    }

    @Test
    fun onCardSelection_chapterCard() {
        val chapterCard = CardData(chapter2)

        activeChapterListener = createChangeListener {
            Assert.assertEquals(chapterCard.chapterSource, it)
        }
        workbookDataStore.activeChapterProperty.addListener(activeChapterListener)

        activeChunkListener = createChangeListener {
            Assert.assertNull(it)
        }
        workbookDataStore.activeChunkProperty.addListener(activeChunkListener)

        chapterPageViewModel.onCardSelection(chapterCard)
    }

    @Test
    fun onCardSelection_chunkCard() {
        val chunkCard = CardData(chunk1)

        activeChunkListener = createChangeListener {
            Assert.assertEquals(chunkCard.chunkSource, it)
        }
        workbookDataStore.activeChunkProperty.addListener(activeChunkListener)

        chapterPageViewModel.onCardSelection(chunkCard)
    }

    @Test
    fun checkCanCompile_someSelected() {
        canCompileListener = createChangeListener {
            Assert.assertEquals(false, it)
        }
        chapterPageViewModel.canCompileProperty.addListener(canCompileListener)

        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chunk1.audio.insertTake(take)
        chunk1.audio.selectTake(take)
        chunk2.audio.insertTake(take)
        chunk2.audio.selectTake(null)

        chapterPageViewModel.checkCanCompile()
    }

    @Test
    fun checkCanCompile_allSelected() {
        canCompileListener = createChangeListener {
            Assert.assertEquals(true, it)
        }
        chapterPageViewModel.canCompileProperty.addListener(canCompileListener)

        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chunk1.audio.insertTake(take)
        chunk1.audio.selectTake(take)
        chunk2.audio.insertTake(take)
        chunk2.audio.selectTake(take)

        chapterPageViewModel.checkCanCompile()
    }

    @Test
    fun setWorkChunk_initially() {
        val chunkCard = CardData(chunk1)

        noTakesListener = createChangeListener {
            Assert.assertEquals(true, it)
        }
        chapterPageViewModel.noTakesProperty.addListener(noTakesListener)

        workChunkListener = createChangeListener {
            Assert.assertEquals(chunkCard, it)
        }
        chapterPageViewModel.workChunkProperty.addListener(workChunkListener)

        chapterPageViewModel.setWorkChunk()
    }

    @Test
    fun setWorkChunk_secondChunk() {
        val chunkCard = CardData(chunk2)

        noTakesListener = createChangeListener {
            Assert.assertEquals(false, it)
        }
        chapterPageViewModel.noTakesProperty.addListener(noTakesListener)

        workChunkListener = createChangeListener {
            Assert.assertEquals(chunkCard, it)
        }
        chapterPageViewModel.workChunkProperty.addListener(workChunkListener)

        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chunk1.audio.insertTake(take)
        chunk1.audio.selectTake(take)

        chapterPageViewModel.setWorkChunk()
    }

    @Test
    fun setSelectedChapterTake_takeSelected() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chapter1.audio.insertTake(take)
        chapter1.audio.selectTake(take)

        selectedChapterTakeListener = createChangeListener {
            Assert.assertEquals(take, it)
        }
        chapterPageViewModel.selectedChapterTakeProperty.addListener(selectedChapterTakeListener)
    }

    @Test
    fun recordChapter_recordsTake1() {
        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.RECORDER, it)
        }
        chapterPageViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        chapterPageViewModel.recordChapter()
    }

    @Test
    fun processTakeWithPlugin_recorder() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.RECORDER, it)
        }
        chapterPageViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        chapter1.audio.insertTake(take)
        chapter1.audio.selectTake(take)

        chapterPageViewModel.processTakeWithPlugin(PluginType.RECORDER)
    }

    @Test
    fun processTakeWithPlugin_editor() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.EDITOR, it)
        }
        chapterPageViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        chapter1.audio.insertTake(take)
        chapter1.audio.selectTake(take)

        chapterPageViewModel.processTakeWithPlugin(PluginType.EDITOR)
    }

    @Test
    fun processTakeWithPlugin_marker() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chapter1.audio.insertTake(take)
        chapter1.audio.selectTake(take)

        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.MARKER, it)
        }
        chapterPageViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        chapterPageViewModel.processTakeWithPlugin(PluginType.MARKER)
    }

    @Test
    fun processTakeWithPlugin_noSelectedTake() {
        contextListener = createChangeListener {
            Assert.assertEquals(null, it)
        }
        chapterPageViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(0, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        chapterPageViewModel.processTakeWithPlugin(PluginType.EDITOR)
    }

    @Test
    fun compile_updateProperty() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chunk1.audio.insertTake(take)
        chunk1.audio.selectTake(take)
        chunk2.audio.insertTake(take)
        chunk2.audio.selectTake(take)

        val file = directoryProvider.createTempFile("test", ".wav")
        takeFile.copyTo(file, true)

        chapterPageViewModel.concatenateAudio = mock {
            on { execute(any()) } doReturn Single.just(file)
        }

        var counter = 1
        isCompilingListener = createChangeListener {
            when (counter) {
                1 -> Assert.assertEquals(true, it)
                2 -> Assert.assertEquals(false, it)
            }
            counter++
        }
        chapterPageViewModel.isCompilingProperty.addListener(isCompilingListener)

        chapterPageViewModel.checkCanCompile()
        chapterPageViewModel.compile()
    }

    @Test
    fun exportChapter_updateProperty() {
        val take = Take("test.wav", takeFile, 1, MimeType.WAV, LocalDate.now())

        chapter1.audio.insertTake(take)
        chapter1.audio.selectTake(take)
        chapterPageViewModel.audioConverter = mock {
            on { wavToMp3(any(), any(), any()) } doReturn Completable.complete()
        }

        var counter = 1
        showExportProgressListener = createChangeListener {
            when (counter) {
                1 -> Assert.assertEquals(true, it)
                2 -> Assert.assertEquals(false, it)
            }
            counter++
        }
        chapterPageViewModel.showExportProgressDialogProperty.addListener(showExportProgressListener)

        chapterPageViewModel.exportChapter(File("test"))
    }

    @Test
    fun dialogTitleBinding_pluginTitle() {
        val stringProperty = SimpleStringProperty()
        stringProperty.bind(chapterPageViewModel.dialogTitleBinding())

        chapterPageViewModel.contextProperty.set(PluginType.RECORDER)
        settingsViewModel.selectedRecorderProperty.set(recorderPlugin)
        workbookDataStore.activeTakeNumberProperty.set(1)

        Assert.assertEquals("Take 01 opened in Recorder", stringProperty.value)
    }

    @Test
    fun dialogTextBinding_pluginText() {
        val expected = "Orature will be unavailable while take 01 is open in Recorder. " +
                "Finish your work in Recorder to continue using Orature."

        val stringProperty = SimpleStringProperty()
        stringProperty.bind(chapterPageViewModel.dialogTextBinding())

        chapterPageViewModel.contextProperty.set(PluginType.RECORDER)
        settingsViewModel.selectedRecorderProperty.set(recorderPlugin)
        workbookDataStore.activeTakeNumberProperty.set(1)

        Assert.assertEquals(expected, stringProperty.value)
    }
}
