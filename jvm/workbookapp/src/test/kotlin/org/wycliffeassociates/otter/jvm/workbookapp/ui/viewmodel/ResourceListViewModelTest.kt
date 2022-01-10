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
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.testfx.api.FxToolkit
import org.testfx.util.WaitForAsyncUtils
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.AssociatedAudio
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Chunk
import org.wycliffeassociates.otter.common.data.workbook.Resource
import org.wycliffeassociates.otter.common.data.workbook.ResourceGroup
import org.wycliffeassociates.otter.common.data.workbook.TextItem
import tornadofx.*
import java.io.File
import java.time.LocalDate

class ResourceListViewModelTest {

    companion object {
        private val testApp = TestApp()
        private lateinit var resourceListViewModel: ResourceListViewModel
        private lateinit var workbookDataStore: WorkbookDataStore
        private lateinit var recordResourceViewModel: RecordResourceViewModel

        private val english = Language(
            "en",
            "English",
            "English",
            "ltr",
            isGateway = true,
            region = "Europe"
        )
        private val resourceMetadataTn = ResourceMetadata(
            conformsTo = "rc0.2",
            creator = "Door43 World Missions Community",
            description = "Description",
            format = "text/markdown",
            identifier = "tn",
            issued = LocalDate.now(),
            language = english,
            modified = LocalDate.now(),
            publisher = "unfoldingWord",
            subject = "Translator Notes",
            type = ContainerType.Help,
            title = "translationNotes",
            version = "1",
            license = "",
            path = File(".")
        )

        private val chapterResourceGroup = ResourceGroup(
            resourceMetadataTn,
            Observable.fromIterable(
                listOf(
                    Resource(
                        title = createTitleComponent(1, "gen_1_s1"),
                        body = null
                    ),
                    Resource(
                        title = createTitleComponent(3, "gen_1_s3"),
                        body = createBodyComponent(4, "gen_1_s4")
                    ),
                    Resource(
                        title = createTitleComponent(5, "gen_1_s5"),
                        body = createBodyComponent(6, "gen_1_s6")
                    )
                )
            )
        )

        private val chunk1ResourceGroup = ResourceGroup(
            resourceMetadataTn,
            Observable.fromIterable(
                listOf(
                    Resource(
                        title = createTitleComponent(1, "gen_1_v1_s1"),
                        body = null
                    ),
                    Resource(
                        title = createTitleComponent(3, "gen_1_v1_s3"),
                        body = createBodyComponent(4, "gen_1_v1_s4")
                    )
                )
            )
        )

        private val chunk2ResourceGroup = ResourceGroup(
            resourceMetadataTn,
            Observable.fromIterable(
                listOf(
                    Resource(
                        title = createTitleComponent(1, "gen_1_v2_s1"),
                        body = createBodyComponent(2, "gen_1_v2_s2")
                    ),
                    Resource(
                        title = createTitleComponent(3, "gen_1_v2_s3"),
                        body = createBodyComponent(4, "gen_1_v2_s4")
                    ),
                    Resource(
                        title = createTitleComponent(5, "gen_1_v2_s5"),
                        body = createBodyComponent(6, "gen_1_v2_s6")
                    )
                )
            )
        )

        private val chunk1 = Chunk(
            sort = 1,
            audio = createAssociatedAudio(),
            textItem = TextItem("Chunk 1", MimeType.USFM),
            start = 1,
            end = 1,
            contentType = ContentType.TEXT,
            resources = listOf(chunk1ResourceGroup),
            label = "Chunk"
        )

        private val chunk2 = Chunk(
            sort = 2,
            audio = createAssociatedAudio(),
            textItem = TextItem("Chunk 2", MimeType.USFM),
            start = 2,
            end = 2,
            contentType = ContentType.TEXT,
            resources = listOf(chunk2ResourceGroup),
            label = "Chunk"
        )

        private val testChapter = Chapter(
            sort = 1,
            title = "gen_1",
            audio = createAssociatedAudio(),
            subtreeResources = listOf(resourceMetadataTn),
            resources = listOf(chapterResourceGroup),
            chunks = Observable.fromIterable(
                listOf(
                    chunk1,
                    chunk2
                )
            ),
            label = "Chapter"
        )

        private val testResourceNoBody = Resource(
            title = createTitleComponent(1, "gen_1_v1_s1"),
            body = null
        )

        private val testResourceWithBody = Resource(
            title = createTitleComponent(1, "gen_1_v1_s1"),
            body = createTitleComponent(2, "gen_1_v1_s2")
        )

        private fun getResourceGroupSize(idx: Int): Long {
            return resourceListViewModel.resourceGroupCardItemList[idx].resources.count().blockingGet()
        }

        private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create())
        private fun createTitleComponent(sort: Int, title: String) = Resource.Component(
            sort,
            TextItem(title, MimeType.MARKDOWN),
            createAssociatedAudio(),
            ContentType.TITLE,
            "Title"
        )
        private fun createBodyComponent(sort: Int, title: String) = Resource.Component(
            sort,
            TextItem(title, MimeType.MARKDOWN),
            createAssociatedAudio(),
            ContentType.BODY,
            "Body"
        )

        @BeforeClass
        @JvmStatic fun setup() {
            FxToolkit.registerPrimaryStage()
            FxToolkit.setupApplication { testApp }

            resourceListViewModel = find()

            workbookDataStore = find()
            workbookDataStore.activeResourceMetadataProperty.set(resourceMetadataTn)

            recordResourceViewModel = find()
        }

        @AfterClass
        fun tearDown() {
            FxToolkit.hideStage()
            FxToolkit.cleanupStages()
            FxToolkit.cleanupApplication(testApp)
        }
    }

    @Test
    fun `setActiveChunkAndRecordables sets BookElement`() {
        resourceListViewModel.setActiveChunkAndRecordables(chunk1, testResourceNoBody)

        Assert.assertEquals(chunk1, workbookDataStore.activeChunkProperty.value)
    }

    @Test
    fun `setActiveChunkAndRecordable calls setRecordableListItems`() {
        val spiedRecordResourceViewModel = spy(recordResourceViewModel)
        val spiedResourcesViewModel = spy(resourceListViewModel)
        whenever(spiedResourcesViewModel.recordResourceViewModel).thenReturn(spiedRecordResourceViewModel)

        // Resource with just a title
        spiedResourcesViewModel.setActiveChunkAndRecordables(chunk1, testResourceNoBody)

        verify(spiedRecordResourceViewModel, times(1))
            .setRecordableListItems(listOf(testResourceNoBody.title))
        // TODO: This seems like an integration test
        Assert.assertEquals(1, spiedRecordResourceViewModel.recordableList.size)

        // Resource with title and body
        spiedResourcesViewModel.setActiveChunkAndRecordables(chunk1, testResourceWithBody)

        verify(spiedRecordResourceViewModel, times(1))
            .setRecordableListItems(listOf(testResourceWithBody.title, testResourceWithBody.body!!))
        // TODO: This seems like an integration test
        Assert.assertEquals(2, spiedRecordResourceViewModel.recordableList.size)
    }

    @Test
    fun `test LoadResourceGroups puts appropriate groups in list`() {
        resourceListViewModel.loadResourceGroups(testChapter)

        WaitForAsyncUtils.waitForFxEvents()

        Assert.assertEquals(3, getResourceGroupSize(0))
        Assert.assertEquals(2, getResourceGroupSize(1))
        Assert.assertEquals(3, getResourceGroupSize(2))

        Assert.assertEquals(3, resourceListViewModel.resourceGroupCardItemList.size)
    }
}
