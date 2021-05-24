package org.wycliffeassociates.otter.common.persistence.repositories

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import java.time.LocalDate
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.DateHolder
import org.wycliffeassociates.otter.common.data.workbook.ResourceGroup
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.TakeHolder
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository.IDatabaseAccessors

class TestWorkbookRepository {
    /** When a unique ID is needed, just use this. */
    private var autoincrement: Int = 1
        get() = field++

    private val english = Language(
        "en",
        "English",
        "English",
        "ltr",
        isGateway = true,
        region = "Europe",
        id = autoincrement
    )
    private val latin = Language(
        "la",
        "Latin",
        "Latin",
        "ltr",
        isGateway = false,
        region = "Europe",
        id = autoincrement
    )

    private val rcBase = ResourceMetadata(
        conformsTo = "rc0.2",
        creator = "Door43 World Missions Community",
        description = "Description",
        format = "text/usfm",
        identifier = "ulb",
        issued = LocalDate.now(),
        language = english,
        modified = LocalDate.now(),
        publisher = "unfoldingWord",
        subject = "Bible",
        type = ContainerType.Bundle,
        title = "Unlocked Literal Bible",
        version = "1",
        path = File(".")
    )
    private val rcSource = rcBase.copy(id = autoincrement, language = english)
    private val rcTarget = rcBase.copy(id = autoincrement, language = latin)

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

    private val collectionBase = Collection(
        sort = 1,
        slug = "gen",
        labelKey = "project",
        titleKey = "Genesis",
        resourceContainer = null
    )
    private val collSource = collectionBase.copy(resourceContainer = rcSource, id = autoincrement)
    private val collTarget = collectionBase.copy(resourceContainer = rcTarget, id = autoincrement)

    private fun buildWorkbook(
        db: IDatabaseAccessors,
        source: Collection = collSource,
        target: Collection = collTarget
    ) = WorkbookRepository(db).get(source, target)

    private fun resourceSlugArray(resourceMetadatas: List<ResourceMetadata>) =
        resourceMetadatas
            .map(ResourceMetadata::identifier)
            .sorted()
            .toTypedArray()

    private fun resourceSlugArray(resourceGroups: Iterable<ResourceGroup>) =
        resourceSlugArray(resourceGroups.map { it.metadata })

    private fun buildBasicTestDb(): IDatabaseAccessors = mock()

    private object BasicTestParams {
        const val chaptersPerBook = 3
        const val chunksPerChapter = 5
    }

    private fun buildBasicTestWorkbook(mockedDb: IDatabaseAccessors = buildBasicTestDb()): Workbook {
        whenever(
            mockedDb.getChildren(any())
        ).thenAnswer { invocation ->
            val collection = invocation.getArgument<Collection>(0)!!
            Single.just(
                when (collection.slug.count { it == '_' }) {
                    0 -> {
                        (1..BasicTestParams.chaptersPerBook).map { chapter ->
                            Collection(
                                sort = chapter,
                                slug = collection.slug + "_" + chapter,
                                id = autoincrement,
                                resourceContainer = collection.resourceContainer,
                                titleKey = chapter.toString(),
                                labelKey = ContentLabel.CHAPTER.value
                            )
                        }
                    }
                    else -> emptyList()
                }
            )
        }

        whenever(
            mockedDb.getContentByCollection(any())
        ).thenAnswer { invocation ->
            val collection = invocation.getArgument<Collection>(0)!!
            val format = if (collection.resourceContainer == rcTarget) "audio/wav" else "text/usfm"
            Single.just(
                when (collection.slug.count { it == '_' }) {
                    1 -> {
                        (1..BasicTestParams.chunksPerChapter).map { verse ->
                            Content(
                                id = autoincrement,
                                start = verse,
                                end = verse,
                                sort = verse,
                                labelKey = ContentLabel.VERSE.value,
                                type = ContentType.TEXT,
                                format = format,
                                text = "/v $verse but test everything; hold fast what is good.",
                                selectedTake = null
                            )
                        }
                    }
                    else -> emptyList()
                }
            )
        }

        whenever(
            mockedDb.getCollectionMetaContent(any())
        ).thenReturn(
            Single.just(
                Content(
                    sort = 0,
                    labelKey = ContentLabel.CHAPTER.value,
                    start = 1,
                    end = BasicTestParams.chunksPerChapter,
                    selectedTake = null,
                    text = null,
                    format = "WAV",
                    type = ContentType.META,
                    id = autoincrement
                )
            )
        )

        whenever(
            mockedDb.getSubtreeResourceMetadata(any())
        ).thenAnswer { invocation ->
            val collection = invocation.getArgument<Collection>(0)!!
            when (rcSource.id) {
                collection.resourceContainer?.id -> listOf(resourceMetadataTn)
                else -> emptyList()
            }
        }

        whenever(
            mockedDb.getResourceMetadata(any<Collection>())
        ).thenReturn(
            listOf(resourceMetadataTn)
        )

        whenever(
            mockedDb.getResourceMetadata(any<Content>())
        ).thenReturn(
            listOf(resourceMetadataTn)
        )

        whenever(
            mockedDb.getResources(any<Content>(), any())
        ).thenAnswer { invocation ->
            val content = invocation.getArgument<Content>(0)!!
            val metadata = invocation.getArgument<ResourceMetadata>(1)!!
            if (content.start == 2 && metadata == resourceMetadataTn) {
                Observable.fromArray(
                    Content(
                        id = autoincrement,
                        start = content.start,
                        end = content.end,
                        sort = 1,
                        labelKey = ContentLabel.HELP_TITLE.value,
                        type = ContentType.TITLE,
                        format = "text/markdown",
                        text = "but test everything; hold fast what is good.",
                        selectedTake = null
                    ),
                    Content(
                        id = autoincrement,
                        start = content.start,
                        end = content.end,
                        sort = 2,
                        labelKey = ContentLabel.HELP_BODY.value,
                        type = ContentType.BODY,
                        format = "text/markdown",
                        text = "The original author may not have had TDD in mind.",
                        selectedTake = null
                    )
                )
            } else {
                Observable.empty()
            }
        }

        whenever(
            mockedDb.getResources(any<Collection>(), any())
        ).thenAnswer { invocation ->
            val collection = invocation.getArgument<Collection>(0)!!
            val metadata = invocation.getArgument<ResourceMetadata>(1)!!
            if (collection.titleKey == "2" && metadata == resourceMetadataTn) {
                Observable.fromArray(
                    Content(
                        id = autoincrement,
                        start = 1,
                        end = BasicTestParams.chunksPerChapter,
                        sort = 1,
                        labelKey = ContentLabel.HELP_TITLE.value,
                        type = ContentType.TITLE,
                        format = "text/markdown",
                        text = "Chapter 2 notes",
                        selectedTake = null
                    ),
                    Content(
                        id = autoincrement,
                        start = 1,
                        end = BasicTestParams.chunksPerChapter,
                        sort = 2,
                        labelKey = ContentLabel.HELP_BODY.value,
                        type = ContentType.BODY,
                        format = "text/markdown",
                        text = "Chapter 2 is a fine chapter. Here are the notes.",
                        selectedTake = null
                    )
                )
            } else {
                Observable.empty()
            }
        }

        whenever(
            mockedDb.getTakeByContent(any())
        ).thenAnswer { invocation ->
            val content = invocation.getArgument<Content>(0)!!
            val take = if (content.format == "audio/wav" && content.start == 3) {
                val id = autoincrement
                org.wycliffeassociates.otter.common.data.primitives.Take(
                    number = id,
                    id = id,
                    path = File("."),
                    filename = ".",
                    markers = listOf(),
                    played = false,
                    created = LocalDate.now(),
                    deleted = null
                )
            } else {
                null
            }
            Single.just(listOfNotNull(take))
        }

        whenever(
            mockedDb.insertTakeForContent(any(), any())
        ).thenReturn(
            Single.just(autoincrement)
        )

        whenever(
            mockedDb.deleteTake(any(), any())
        ).thenReturn(
            Completable.complete()
        )

        whenever(
            mockedDb.updateContent(any())
        ).thenReturn(
            Completable.complete()
        )

        return buildWorkbook(mockedDb)
    }

    @Test
    fun workbookHasBooksAndLanguageSlugs() {
        val workbook = buildBasicTestWorkbook()

        Assert.assertEquals(1, workbook.source.sort)
        Assert.assertEquals(1, workbook.target.sort)
        Assert.assertEquals("Genesis", workbook.source.title)
        Assert.assertEquals("Genesis", workbook.target.title)
        Assert.assertArrayEquals(arrayOf("tn"), resourceSlugArray(workbook.source.subtreeResources))
        Assert.assertArrayEquals(arrayOf(), resourceSlugArray(workbook.target.subtreeResources))
        Assert.assertEquals("en", workbook.source.language.slug)
        Assert.assertEquals("la", workbook.target.language.slug)
    }

    @Test
    fun chaptersAreLazyLoad() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)

        // Load some things that shouldn't trigger chapter fetch, and verify no DB call is made
        Assert.assertEquals("Genesis", workbook.source.title)
        Assert.assertArrayEquals(arrayOf("tn"), resourceSlugArray(workbook.source.subtreeResources))
        verify(mockedDb, times(0)).getChildren(any())

        // Fetch chapters, and verify one DB call is made
        workbook.source.chapters.blockingLast()
        verify(mockedDb, times(1)).getChildren(any())
    }

    @Test
    fun chaptersAreCached() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)

        // Fetch chapters twice, and verify only one DB call is made
        workbook.source.chapters.blockingLast()
        workbook.source.children.blockingLast()
        verify(mockedDb, times(1)).getChildren(any())
    }

    @Test
    fun chaptersIsAliasOfBookChildren() {
        val workbook = buildBasicTestWorkbook()

        Assert.assertArrayEquals(
            workbook.source.chapters.blockingIterable().toList().toTypedArray(),
            workbook.source.children.blockingIterable().toList().toTypedArray()
        )
    }

    @Test
    fun chunksAreLazyLoad() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!

        // Load some things that shouldn't trigger chunk fetch, and verify no DB call is made
        Assert.assertEquals(1, chapter.sort)
        Assert.assertArrayEquals(arrayOf("tn"), resourceSlugArray(chapter.resources))
        verify(mockedDb, times(0)).getContentByCollection(any())

        // Fetch chunks, and verify one DB call is made
        chapter.chunks.blockingLast()
        verify(mockedDb, times(1)).getContentByCollection(any())
    }

    @Test
    fun chunksAreCached() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!

        // Fetch chunks twice, and verify only one DB call is made
        chapter.chunks.blockingLast()
        chapter.children.blockingLast()
        verify(mockedDb, times(1)).getContentByCollection(any())
    }

    @Test
    fun chunksIsAliasOfChapterChildren() {
        val workbook = buildBasicTestWorkbook()
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!

        Assert.assertArrayEquals(
            chapter.children.blockingIterable().sortedBy { it.sort }.toTypedArray(),
            chapter.chunks.blockingIterable().sortedBy { it.sort }.toTypedArray()
        )
    }

    @Test
    fun subtreeResourcesWork() {
        val workbook = buildBasicTestWorkbook()
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!

        Assert.assertArrayEquals(arrayOf(resourceMetadataTn), chapter.subtreeResources.toTypedArray())
    }

    @Test
    fun resourceGroupsHaveCorrectMetadata() {
        val workbook = buildBasicTestWorkbook()
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "2" }.blockingSingle()
        val resourceGroups = chunk.resources

        val expected = 1
        Assert.assertEquals("This chunk should have $expected ResourceGroups", expected, resourceGroups.size)
        Assert.assertEquals("ResourceMetadata", resourceMetadataTn, resourceGroups.first().metadata)
    }

    @Test
    fun resourcesAreLazyLoad() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "2" }.blockingSingle()
        val resourceGroup = chunk.resources.first()

        // Load some things that shouldn't trigger resource fetch, and verify no DB call is made
        Assert.assertEquals(resourceMetadataTn, resourceGroup.metadata)
        verify(mockedDb, times(0)).getResources(any<Content>(), any())

        // Fetch chunks, and verify one DB call is made
        resourceGroup.resources.blockingLast()
        verify(mockedDb, times(1)).getResources(any<Content>(), any())
    }

    @Test
    fun resourceGroupsHaveCorrectResources() {
        val workbook = buildBasicTestWorkbook()
        val chapter = workbook.source.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "2" }.blockingSingle()
        val resourceGroup = chunk.resources.firstOrNull()
        Assert.assertNotNull(resourceGroup)
        val resources = resourceGroup!!.resources.blockingIterable().toList()

        val expected = 1
        Assert.assertEquals("This chunk should have $expected Resources", expected, resources.size)
        Assert.assertTrue("Expected resource text", resources.first().body?.textItem?.text?.contains("TDD") ?: false)
    }

    @Test
    fun pushingToTakesRelayCallsDbWrite() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.target.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.blockingFirst()
        val takes = chunk.audio.takes

        // Verify precondition - no DB writes yet
        verify(mockedDb, times(0)).insertTakeForContent(any(), any())

        // Push a new take, and verify the DB is called
        val take = Take(
            name = "TakeName",
            file = File("."),
            format = MimeType.WAV,
            number = autoincrement,
            createdTimestamp = LocalDate.now()
        )
        takes.accept(take)
        verify(mockedDb, times(1)).insertTakeForContent(any(), any())
    }

    @Test
    fun deletingTakeCallsDbWrite() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.target.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "3" }.blockingSingle()
        val takes = chunk.audio.takes
        val take = takes.blockingFirst()

        // Verify precondition - no DB writes yet
        verify(mockedDb, times(0)).deleteTake(any(), any())

        // Delete a take, and verify the DB is called
        take.deletedTimestamp.accept(DateHolder(LocalDate.now()))
        verify(mockedDb, times(1)).deleteTake(any(), any())
    }

    @Test
    fun settingSelectedTakeCallsDbWrite() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.target.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "3" }.blockingSingle()
        val takes = chunk.audio.takes
        val take = takes.blockingFirst()

        // Verify precondition - no DB writes yet
        verify(mockedDb, times(0)).updateContent(any())

        // Select a take, and verify the DB is called
        chunk.audio.selected.accept(TakeHolder(take))
        verify(mockedDb, times(1)).updateContent(any())
    }

    @Test
    fun deletingSelectedTakeResetsSelection() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.target.chapters.blockingIterable().minBy { it.sort }!!
        val chunk = chapter.chunks.filter { it.title == "3" }.blockingSingle()
        val takes = chunk.audio.takes
        val take = takes.blockingFirst()

        // Select a take to set up the test, and verify the preconditions
        chunk.audio.selected.accept(TakeHolder(take))
        verify(mockedDb, times(1)).updateContent(any())
        verify(mockedDb, times(0)).deleteTake(any(), any())
        Assert.assertNotNull("Selection should be non-null", chunk.audio.selected.value?.value)

        // Delete the take, and confirm the selection is cleared
        take.deletedTimestamp.accept(DateHolder.now())
        verify(mockedDb, times(2)).updateContent(any())
        verify(mockedDb, times(1)).deleteTake(any(), any())
        Assert.assertNull("Selection should be null", chunk.audio.selected.value?.value)
    }

    @Test
    fun textItemsHaveCorrectValues() {
        val mockedDb = buildBasicTestDb()
        val workbook = buildBasicTestWorkbook(mockedDb)
        val chapter = workbook.source.chapters.blockingFirst()
        val chunks = chapter.chunks.blockingIterable().sortedBy { it.sort }

        Assert.assertArrayEquals(
            "Expected chunk titles",
            (1..BasicTestParams.chunksPerChapter).map(Int::toString).toTypedArray(),
            chunks.map { it.title }.toTypedArray()
        )
        chunks.forEach {
            Assert.assertTrue(
                "Chunk text expected",
                it.textItem.text.startsWith("/v ${it.title}")
            )
        }
    }
}
