package org.wycliffeassociates.otter.jvm.app.ui.resources.viewmodel

import com.jakewharton.rxrelay2.ReplayRelay
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.data.workbook.*
import org.wycliffeassociates.otter.jvm.app.ui.resourcetakes.viewmodel.RecordResourceViewModel
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import tornadofx.*

class ResourcesViewModelTest : ViewModel() {
    private val resourcesViewModel: ResourcesViewModel by inject()
    private val workbookViewModel: WorkbookViewModel by inject()
    private val recordResourceViewModel: RecordResourceViewModel by inject()

    init {
        workbookViewModel.activeResourceSlugProperty.set("tn")
    }

    @Test
    fun navigateToTakesPage_setsBookElement() {
        resourcesViewModel.navigateToTakesPage(chunk1, testResourceNoBody)

        Assert.assertEquals(chunk1, workbookViewModel.activeChunkProperty.value)
    }

    @Test
    fun navigateToTakesPage_callsSetRecordableListItems() {
        val spiedRecordResourceViewModel = spy(recordResourceViewModel)
        val spiedResourcesViewModel = spy(resourcesViewModel)
        whenever(spiedResourcesViewModel.recordResourceViewModel).thenReturn(spiedRecordResourceViewModel)

        // Resource with just a title
        spiedResourcesViewModel.navigateToTakesPage(chunk1, testResourceNoBody)

        verify(spiedRecordResourceViewModel, times(1))
            .setRecordableListItems(listOf(testResourceNoBody.title))
        // TODO: This seems like an integration test
        Assert.assertEquals(1, spiedRecordResourceViewModel.recordableList.size)

        // Resource with title and body
        spiedResourcesViewModel.navigateToTakesPage(chunk1, testResourceWithBody)

        verify(spiedRecordResourceViewModel, times(1))
            .setRecordableListItems(listOf(testResourceWithBody.title, testResourceWithBody.body!!))
        // TODO: This seems like an integration test
        Assert.assertEquals(2, spiedRecordResourceViewModel.recordableList.size)
    }

    @Test
    fun testLoadResourceGroups_putsAppropriateGroupsInList() {
        workbookViewModel.activeChapterProperty.set(testChapter)

        resourcesViewModel.loadResourceGroups()

        Assert.assertEquals(3, resourcesViewModel.resourceGroupCardItemList.size)

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
        return resourcesViewModel.resourceGroupCardItemList[idx].resources.count().blockingGet()
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
        ResourceInfo("tn", "translationNotes"),
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
        ResourceInfo("tn", "translationNotes"),
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
        ResourceInfo("tn", "translationNotes"),
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
        subtreeResources = listOf(ResourceInfo("tn", "translationNotes")),
        resources = listOf(chapterResourceGroup),
        chunks = Observable.fromIterable(
            listOf(
                chunk1,
                chunk2
            )
        )
    )
}