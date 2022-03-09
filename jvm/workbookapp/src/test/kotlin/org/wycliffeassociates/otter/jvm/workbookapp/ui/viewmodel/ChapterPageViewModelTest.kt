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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
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
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.content.TakeActions
import org.wycliffeassociates.otter.common.domain.plugins.AudioPluginData
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPlugin
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.utils.writeWavFile
import tornadofx.*
import java.io.File
import java.time.LocalDate
import java.util.*

class ChapterPageViewModelTest {
//    companion object {
//        private val testApp: TestApp = TestApp()
//
//        private lateinit var chapterPageViewModel: ChapterPageViewModel
//        private lateinit var audioPluginViewModel: AudioPluginViewModel
//        private lateinit var settingsViewModel: SettingsViewModel
//        private lateinit var workbookDataStore: WorkbookDataStore
//
//        private var canCompileListener: ChangeListener<Boolean>? = null
//        private var isCompilingListener: ChangeListener<Boolean>? = null
//        private var contextListener: ChangeListener<PluginType>? = null
//        private var activeChapterListener: ChangeListener<Chapter>? = null
//        private var activeChunkListener: ChangeListener<Chunk>? = null
//        private var noTakesListener: ChangeListener<Boolean>? = null
//        private var activeTakeNumberListener: ChangeListener<Number>? = null
//        private var workChunkListener: ChangeListener<CardData>? = null
//        private var selectedChapterTakeListener: ChangeListener<Take>? = null
//        private var showExportProgressListener: ChangeListener<Boolean>? = null
//
//        private val directoryProvider = testApp.dependencyGraph.injectDirectoryProvider()
//        private val tempDir = directoryProvider.tempDirectory
//        private lateinit var take1File: File
//        private lateinit var take2File: File
//        private var sourceTakeFile = File(tempDir, "sourceTake.wav")
//
//        private var chunk1 = createChunk(1)
//        private var chunk2 = createChunk(2)
//
//        private var chapter1 = createChapter(1)
//
//        private val english = Language(
//            "en",
//            "English",
//            "English",
//            "ltr",
//            true,
//            "Europe"
//        )
//
//        private val resourceMetadata = mock<ResourceMetadata> {
//            on { identifier } doReturn "ulb"
//        }
//
//        private val book = mock<Book> {
//            on { resourceMetadata } doReturn resourceMetadata
//            on { chapters } doReturn Observable.fromIterable(listOf(chapter1/*, chapter2*/))
//            on { language } doReturn english
//            on { slug } doReturn "gen"
//        }
//
//        private val sourceAudioAccessor = mock<SourceAudioAccessor> {
//            on { getChapter(any()) } doReturn SourceAudio(sourceTakeFile, 0, 1)
//        }
//
//        private val workbook = mock<Workbook> {
//            on { source } doReturn book
//            on { target } doReturn book
//            on { sourceAudioAccessor } doReturn sourceAudioAccessor
//        }
//
//        private val recorderPlugin = AudioPluginData(
//            1,
//            "Recorder",
//            "1",
//            false,
//            true,
//            false,
//            "",
//            listOf(),
//            null
//        )
//
//        private val projectFilesAccessor = mock<ProjectFilesAccessor> {
//            on { audioDir } doReturn File("test")
//        }
//
//        private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())
//
//        private fun createChunk(number: Int): Chunk {
//            return Chunk(
//                sort = number,
//                audio = createAssociatedAudio(),
//                textItem = TextItem("Chunk $number", MimeType.USFM),
//                start = number,
//                end = number,
//                contentType = ContentType.TEXT,
//                resources = listOf(),
//                label = "Chunk"
//            )
//        }
//
//        private fun createChapter(number: Int): Chapter {
//            chunk1 = createChunk(1)
//            chunk2 = createChunk(2)
//            return Chapter(
//                number,
//                "$number",
//                "$number",
//                createAssociatedAudio(),
//                listOf(),
//                listOf(),
//                Observable.fromIterable(listOf(chunk1, chunk2))
//            )
//        }
//
//        private val audioPlugin = mock<IAudioPlugin> {
//            on { isNativePlugin() } doReturn true
//            on { launch(any(), any()) } doReturn Completable.complete()
//        }
//
//        private val takeActions = mock<TakeActions> {
//            on { record(any(), any(), any(), any()) } doReturn Single.just(TakeActions.Result.SUCCESS)
//            on { mark(any(), any(), any()) } doReturn Single.just(TakeActions.Result.SUCCESS)
//            on { edit(any(), any(), any()) } doReturn Single.just(TakeActions.Result.SUCCESS)
//            on { import(any(), any(), any(), any()) } doReturn Completable.complete()
//        }
//
//        private val pluginRepository = mock<IAudioPluginRepository> {
//            on { getPlugin(any()) } doReturn Maybe.just(audioPlugin)
//        }
//
//        private val translation = mock<Translation>()
//
//        @BeforeClass
//        @JvmStatic fun setup() {
//            FxToolkit.registerPrimaryStage()
//            FxToolkit.setupApplication { testApp }
//
//            writeWavFile(sourceTakeFile)
//
//            val configureAudio = ConfigureAudioSystem(
//                testApp.dependencyGraph.injectConnectionFactory(),
//                testApp.dependencyGraph.injectAudioDeviceProvider(),
//                testApp.dependencyGraph.injectAppPreferencesRepository()
//            )
//            configureAudio.configure()
//
//            workbookDataStore = find()
//            workbookDataStore.activeWorkbookProperty.set(workbook)
//            workbookDataStore.activeChapterProperty.set(chapter1)
//            workbookDataStore.activeProjectFilesAccessorProperty.set(projectFilesAccessor)
//            workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)
//
//            audioPluginViewModel = find()
//            chapterPageViewModel = find()
//            settingsViewModel = find()
//
//            audioPluginViewModel.pluginRepository = pluginRepository
//            audioPluginViewModel.takeActions = takeActions
//        }
//
//        @AfterClass
//        @JvmStatic fun tearDown() {
//            FxToolkit.hideStage()
//            FxToolkit.cleanupStages()
//            FxToolkit.cleanupApplication(testApp)
//        }
//    }
//
//    @Before
//    fun prepare() {
//        chapterPageViewModel.selectedChapterTakeProperty.set(null)
//        chapterPageViewModel.contextProperty.set(PluginType.RECORDER)
//        chapterPageViewModel.noTakesProperty.set(false)
//        chapterPageViewModel.workChunkProperty.set(null)
//        chapterPageViewModel.canCompileProperty.set(false)
//        chapterPageViewModel.isCompilingProperty.set(false)
//
//        take1File = File(tempDir, "take${UUID.randomUUID()}.wav")
//        take2File = File(tempDir, "take${UUID.randomUUID()}.wav")
//        writeWavFile(take1File)
//        writeWavFile(take2File)
//
//        chapter1 = createChapter(1)
//        workbookDataStore.activeChapterProperty.set(chapter1)
//
//        workbookDataStore.activeTakeNumberProperty.set(0)
//        chapterPageViewModel.dock()
//    }
//
//    @After
//    fun cleanup() {
//        take1File.delete()
//        take2File.delete()
//
//        canCompileListener?.let {
//            chapterPageViewModel.canCompileProperty.removeListener(it)
//        }
//        contextListener?.let {
//            chapterPageViewModel.contextProperty.removeListener(it)
//        }
//        activeChapterListener?.let {
//            workbookDataStore.activeChapterProperty.removeListener(it)
//        }
//        activeChunkListener?.let {
//            workbookDataStore.activeChunkProperty.removeListener(it)
//        }
//        noTakesListener?.let {
//            chapterPageViewModel.noTakesProperty.removeListener(it)
//        }
//        activeTakeNumberListener?.let {
//            workbookDataStore.activeTakeNumberProperty.removeListener(it)
//        }
//        workChunkListener?.let {
//            chapterPageViewModel.workChunkProperty.removeListener(it)
//        }
//        selectedChapterTakeListener?.let {
//            chapterPageViewModel.selectedChapterTakeProperty.removeListener(it)
//        }
//        isCompilingListener?.let {
//            chapterPageViewModel.isCompilingProperty.removeListener(it)
//        }
//        showExportProgressListener?.let {
//            chapterPageViewModel.showExportProgressDialogProperty.removeListener(it)
//        }
//    }
//
//    private fun <T> createChangeListener(callback: (T) -> Unit): ChangeListener<T> {
//        return ChangeListener { _, _, value ->
//            callback(value)
//        }
//    }
//

//
//    @Test
//    fun `process take with recorder plugin`() {
//        val take = Take("take1", take1File, 1, MimeType.WAV, LocalDate.now())
//
//        contextListener = createChangeListener {
//            Assert.assertEquals(PluginType.RECORDER, it)
//        }
//        chapterPageViewModel.contextProperty.addListener(contextListener)
//
//        activeTakeNumberListener = createChangeListener {
//            Assert.assertEquals(1, it)
//        }
//        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)
//
//        chapter1.audio.insertTake(take)
//        chapter1.audio.selectTake(take)
//
//        chapterPageViewModel.processTakeWithPlugin(PluginType.RECORDER)
//    }
//
//    @Test
//    fun `process take with editor plugin`() {
//        val take = Take("take1", take1File, 1, MimeType.WAV, LocalDate.now())
//
//        contextListener = createChangeListener {
//            Assert.assertEquals(PluginType.EDITOR, it)
//        }
//        chapterPageViewModel.contextProperty.addListener(contextListener)
//
//        activeTakeNumberListener = createChangeListener {
//            Assert.assertEquals(1, it)
//        }
//        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)
//
//        chapter1.audio.insertTake(take)
//        chapter1.audio.selectTake(take)
//
//        chapterPageViewModel.processTakeWithPlugin(PluginType.EDITOR)
//    }
//
//    @Test
//    fun `process take with marker plugin`() {
//        val take = Take("take1", take1File, 1, MimeType.WAV, LocalDate.now())
//
//        chapter1.audio.insertTake(take)
//        chapter1.audio.selectTake(take)
//
//        contextListener = createChangeListener {
//            Assert.assertEquals(PluginType.MARKER, it)
//        }
//        chapterPageViewModel.contextProperty.addListener(contextListener)
//
//        activeTakeNumberListener = createChangeListener {
//            Assert.assertEquals(1, it)
//        }
//        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)
//
//        chapterPageViewModel.processTakeWithPlugin(PluginType.MARKER)
//    }
//
//    @Test
//    fun `process take with editor plugin fails when there is no selected take`() {
//        contextListener = createChangeListener {
//            Assert.assertEquals(null, it)
//        }
//        chapterPageViewModel.contextProperty.addListener(contextListener)
//
//        activeTakeNumberListener = createChangeListener {
//            Assert.assertEquals(0, it)
//        }
//        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)
//
//        chapterPageViewModel.processTakeWithPlugin(PluginType.EDITOR)
//    }
//
//    @Test
//    fun `process take with marker plugin fails when there is no selected take`() {
//        contextListener = createChangeListener {
//            Assert.assertEquals(null, it)
//        }
//        chapterPageViewModel.contextProperty.addListener(contextListener)
//
//        activeTakeNumberListener = createChangeListener {
//            Assert.assertEquals(0, it)
//        }
//        workbookDataStore.activeTakeNumberProperty.addListener(activeTakeNumberListener)
//
//        chapterPageViewModel.processTakeWithPlugin(PluginType.MARKER)
//    }
//

}
