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
import java.io.File
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
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.persistence.repositories.PluginType
import org.wycliffeassociates.otter.jvm.device.ConfigureAudioSystem
import tornadofx.*

class RecordScriptureViewModelTest {
    companion object {
        private val testApp: TestApp = TestApp()

        private lateinit var recordScriptureViewModel: RecordScriptureViewModel
        private lateinit var workbookDataStore: WorkbookDataStore

        private var contextListener: ChangeListener<PluginType>? = null
        private var activeTakeNumberListener: ChangeListener<Number>? = null

        private val chunk1 = Chunk(
            sort = 1,
            audio = createAssociatedAudio(),
            textItem = TextItem("Chunk 1", MimeType.USFM),
            start = 1,
            end = 1,
            contentType = ContentType.TEXT,
            resources = listOf(),
            label = "Chunk"
        )

        private val chunk2 = Chunk(
            sort = 2,
            audio = createAssociatedAudio(),
            textItem = TextItem("Chunk 2", MimeType.USFM),
            start = 2,
            end = 2,
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
            on { chapters } doReturn Observable.fromIterable(listOf(chapter1))
            on { language } doReturn english
            on { slug } doReturn "gen"
        }

        private val takeFile = File(RecordScriptureViewModelTest::class.java.getResource("/files/test.wav")!!.file)

        private val sourceAudioAccessor = mock<SourceAudioAccessor> {
            on { getChapter(any()) } doReturn SourceAudio(takeFile, 0, 1)
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
            on { audioDir } doReturn File("test")
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

            workbookDataStore = find()
            workbookDataStore.activeWorkbookProperty.set(workbook)
            workbookDataStore.activeChapterProperty.set(chapter1)
            workbookDataStore.activeProjectFilesAccessorProperty.set(projectFilesAccessor)
            workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)

            recordScriptureViewModel = find()
        }
    }

    @Before
    fun prepare() {
        recordScriptureViewModel.contextProperty.set(PluginType.RECORDER)
        workbookDataStore.activeTakeNumberProperty.set(0)
    }

    @After
    fun cleanup() {
        contextListener?.let {
            recordScriptureViewModel.contextProperty.removeListener(it)
        }
        activeTakeNumberListener?.let {
            workbookDataStore.activeTakeNumberProperty.removeListener(it)
        }
    }

    @Test
    fun recordNewTake() {
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
}
