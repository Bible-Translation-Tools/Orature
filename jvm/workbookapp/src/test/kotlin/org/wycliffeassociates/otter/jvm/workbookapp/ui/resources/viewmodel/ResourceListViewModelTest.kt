package org.wycliffeassociates.otter.jvm.workbookapp.ui.resources.viewmodel

import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.*
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.jvm.workbookapp.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*
import java.io.File
import java.time.LocalDate

class ResourceListViewModelTest : ViewModel() {
    private val resourceListViewModel: ResourceListViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()
    private val recordResourceViewModel: RecordResourceViewModel by inject()

    private val english = Language("en", "English", "English", "ltr", isGateway = true)
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
        path = File(".")
    )

    init {
        workbookViewModel.activeResourceMetadataProperty.set(resourceMetadataTn)
    }

    @Test
    fun setActiveChunkAndRecordable_setsBookElement() {
        resourceListViewModel.setActiveChunkAndRecordables(chunk1, testResourceNoBody)

        Assert.assertEquals(chunk1, workbookViewModel.activeChunkProperty.value)
    }

    @Test
    fun setActiveChunkAndRecordable_callsSetRecordableListItems() {
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
    fun testLoadResourceGroups_putsAppropriateGroupsInList() {
        resourceListViewModel.loadResourceGroups(testChapter)

        Assert.assertEquals(3, resourceListViewModel.resourceGroupCardItemList.size)

        Assert.assertEquals(3, getResourceGroupSize(0))
        Assert.assertEquals(2, getResourceGroupSize(1))
        Assert.assertEquals(3, getResourceGroupSize(2))
    }

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

    private fun createAssociatedAudio() = AssociatedAudio(ReplayRelay.create<Take>())
    private fun createTitleComponent(sort: Int, title: String) = Resource.Component(
        sort,
        TextItem(title, MimeType.MARKDOWN),
        createAssociatedAudio(),
        ContentType.TITLE
    )
    private fun createBodyComponent(sort: Int, title: String) = Resource.Component(
        sort,
        TextItem(title, MimeType.MARKDOWN),
        createAssociatedAudio(),
        ContentType.BODY
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
        resources = listOf(chunk1ResourceGroup)
    )

    private val chunk2 = Chunk(
        sort = 2,
        audio = createAssociatedAudio(),
        textItem = TextItem("Chunk 2", MimeType.USFM),
        start = 2,
        end = 2,
        contentType = ContentType.TEXT,
        resources = listOf(chunk2ResourceGroup)
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
        )
    )
}