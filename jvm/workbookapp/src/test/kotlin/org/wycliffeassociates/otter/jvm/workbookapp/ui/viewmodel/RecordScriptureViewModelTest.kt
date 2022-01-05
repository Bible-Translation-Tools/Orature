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
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.controls.card.events.TakeEvent
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.utils.Audio
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.concurrent.Semaphore

class RecordScriptureViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()

        private lateinit var recordScriptureViewModel: RecordScriptureViewModel
        private lateinit var workbookDataStore: WorkbookDataStore

        private var contextListener: ChangeListener<PluginType>? = null
        private var activeTakeNumberListener: ChangeListener<Number>? = null
        private var showImportProgressListener: ChangeListener<Boolean>? = null

        private val directoryProvider = testApp.dependencyGraph.injectDirectoryProvider()
        private val tempDir = directoryProvider.tempDirectory
        private var take1File = File(tempDir, "test1.wav")
        private var take2File = File(tempDir, "test2.wav")
        private var sourceTakeFile = File(tempDir, "sourceTake.wav")

        private var chunk1 = createChunk()
        private var chapter = createChapter()

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
            on { chapters } doReturn Observable.fromIterable(listOf(chapter))
            on { language } doReturn english
            on { slug } doReturn "gen"
            on { title } doReturn "Genesis"
        }

        private val sourceAudioAccessor = mock<SourceAudioAccessor> {
            on { getChapter(any()) } doReturn SourceAudio(sourceTakeFile, 0, 1)
        }

        private val workbook = mock<Workbook> {
            on { source } doReturn book
            on { target } doReturn book
            on { sourceAudioAccessor } doReturn sourceAudioAccessor
        }

        private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())

        private fun <T> createChangeListener(callback: (T) -> Unit): ChangeListener<T> {
            return ChangeListener { _, _, value ->
                callback(value)
            }
        }

        private val projectFilesAccessor = mock<ProjectFilesAccessor> {
            on { audioDir } doReturn tempDir
        }

        private fun createChunk(): Chunk {
            return Chunk(
                sort = 1,
                audio = createAssociatedAudio(),
                textItem = TextItem("Chunk 1", MimeType.USFM),
                start = 1,
                end = 1,
                contentType = ContentType.TEXT,
                resources = listOf(),
                label = "Chunk"
            )
        }

        private fun createChapter(): Chapter {
            chunk1 = createChunk()
            return Chapter(
                1,
                "1",
                "1",
                createAssociatedAudio(),
                listOf(),
                listOf(),
                Observable.fromIterable(listOf(chunk1))
            )
        }

        @Throws(InterruptedException::class)
        fun waitForRunLater() {
            val semaphore = Semaphore(0)
            Platform.runLater { semaphore.release() }
            semaphore.acquire()
        }

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

            Audio.writeWavFile(sourceTakeFile)

            workbookDataStore = find()
            workbookDataStore.activeWorkbookProperty.set(workbook)
            workbookDataStore.activeChapterProperty.set(chapter)
            workbookDataStore.activeProjectFilesAccessorProperty.set(projectFilesAccessor)
            workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)

            recordScriptureViewModel = find()
        }
    }

    @Before
    fun prepare() {
        recordScriptureViewModel.contextProperty.set(PluginType.RECORDER)
        workbookDataStore.activeTakeNumberProperty.set(0)
        recordScriptureViewModel.showImportProgressDialogProperty.set(false)

        Audio.writeWavFile(take1File)
        Audio.writeWavFile(take2File)
    }

    @After
    fun cleanup() {
        contextListener?.let {
            recordScriptureViewModel.contextProperty.removeListener(it)
        }
        activeTakeNumberListener?.let {
            workbookDataStore.activeTakeNumberProperty.removeListener(it)
        }
        showImportProgressListener?.let {
            recordScriptureViewModel.showImportProgressDialogProperty.removeListener(it)
        }

        directoryProvider.cleanTempDirectory()
        chapter = createChapter()
        workbookDataStore.activeChapterProperty.set(chapter)
    }

    @Test
    fun recordNewTake_recorder() {
        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.RECORDER, it)
        }
        recordScriptureViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        recordScriptureViewModel.recordNewTake()
    }

    @Test
    fun processTakeWithPlugin_editor() {
        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.EDITOR, it)
        }
        recordScriptureViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        val take = Take("take1", take1File, 1, MimeType.USFM, LocalDate.now())
        val takeEvent = TakeEvent(take, { }, TakeEvent.EDIT_TAKE)

        recordScriptureViewModel.processTakeWithPlugin(takeEvent, PluginType.EDITOR)
    }

    @Test
    fun processTakeWithPlugin_marker() {
        contextListener = createChangeListener {
            Assert.assertEquals(PluginType.MARKER, it)
        }
        recordScriptureViewModel.contextProperty.addListener(contextListener)

        activeTakeNumberListener = createChangeListener {
            Assert.assertEquals(1, it)
        }
        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)

        val take = Take("take1", take1File, 1, MimeType.USFM, LocalDate.now())
        val takeEvent = TakeEvent(take, { }, TakeEvent.MARK_TAKE)

        recordScriptureViewModel.processTakeWithPlugin(takeEvent, PluginType.MARKER)
    }

    @Test
    fun selectTake_recordableSelected() {
        val take = Take("take1", take1File, 1, MimeType.USFM, LocalDate.now())

        val initialLastModified = take.file.lastModified()

        recordScriptureViewModel.selectTake(take)

        Assert.assertNotEquals(take.file.lastModified(), initialLastModified)
        Assert.assertEquals(recordScriptureViewModel.recordable?.audio?.selected?.value?.value, take)
    }

    @Test
    fun importTakes_showProgressDialog() {
        val takes = listOf(take1File)

        showImportProgressListener = createChangeListener {
            Assert.assertEquals(true, it)
        }
        recordScriptureViewModel.showImportProgressDialogProperty.addListener(showImportProgressListener)

        recordScriptureViewModel.importTakes(takes)
    }

    @Test
    fun deleteTake_timestamp_updated_and_another_take_selected() {
        val take1 = Take("take1", take1File, 1, MimeType.USFM, LocalDate.now())
        val take2 = Take("take2", take2File, 2, MimeType.USFM, LocalDate.now())
        val initialDeletedTimestamp = take1.deletedTimestamp.value?.value

        chapter.audio.insertTake(take1)
        chapter.audio.selectTake(take1)
        chapter.audio.insertTake(take2)

        recordScriptureViewModel.loadTakes()
        recordScriptureViewModel.deleteTake(take1)

        waitForRunLater()

        Assert.assertNotEquals(take1.deletedTimestamp.value?.value, initialDeletedTimestamp)
        Assert.assertEquals(1, recordScriptureViewModel.takeCardModels.size)
        Assert.assertEquals(take2, recordScriptureViewModel.recordable?.audio?.selected?.value?.value)
    }
}
