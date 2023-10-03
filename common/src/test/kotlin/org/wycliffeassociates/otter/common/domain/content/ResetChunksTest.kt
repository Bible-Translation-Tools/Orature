package org.wycliffeassociates.otter.common.domain.content

import com.jakewharton.rxrelay2.BehaviorRelay
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.*
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDatabaseAccessors
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.DublinCore
import java.io.File
import java.time.LocalDate

class ResetChunksTest {

    @JvmField @Rule
    val tempDir = TemporaryFolder()
    @JvmField @Rule
    val projectDir = TemporaryFolder()

    private val mockedDirectoryProvider = mock<IDirectoryProvider>()
    private val mockedDb = mock<IWorkbookDatabaseAccessors>()

    private lateinit var projectFilesAccessor: ProjectFilesAccessor
    private lateinit var audioSourceAudioAccessor: SourceAudioAccessor
    private lateinit var sourceAudioDir: File

    private var autoincrement: Int = 1
        get() = field++

    private val english = getEnglishLanguage(autoincrement)
    private val spanish = getSpanishLanguage(autoincrement)

    private val rcBase = getResourceMetadata(english)
    private lateinit var rcSource: ResourceMetadata
    private lateinit var rcTarget: ResourceMetadata
    private lateinit var dublinCore: DublinCore

    private val collectionBase = getGenesisCollection()
    private lateinit var collSource: Collection
    private lateinit var collTarget: Collection

    private lateinit var workbook: Workbook
    private lateinit var chapter: Chapter
    private lateinit var rc: ResourceContainer

    private var clearContentForCollectionTriggered = false

    private object BasicTestParams {
        const val chaptersPerBook = 3
        const val chunksPerChapter = 5
    }

    @Before
    fun setup() {
        mockedDb.apply {
            whenever(
                getTranslation(any(), any())
            ).thenReturn(
                Single.just(
                    Translation(
                        english,
                        spanish,
                        null
                    )
                )
            )
            whenever(
                getChildren(any())
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
                getCollectionMetaContent(any())
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
                        id = autoincrement,
                        draftNumber = 1
                    )
                )
            )
            whenever(
                getTakeByContent(any())
            ).thenAnswer { invocation ->
                val content = invocation.getArgument<Content>(0)!!
                val take = if (content.format == "audio/wav") {
                    val id = autoincrement
                    Take(
                        id = id,
                        number = id,
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
                getChunkCount(any())
            ).thenAnswer { invocation ->
                val collection = invocation.getArgument<Collection>(0)!!
                when (collection.slug.count { it == '_' }) {
                    1 -> {
                        Single.just(BasicTestParams.chunksPerChapter)
                    }
                    else -> {
                        Single.just(0)
                    }
                }
            }
            whenever(getContentByCollectionActiveConnection(any())).thenAnswer { invocation ->
                val collection = invocation.getArgument<Collection>(0)!!
                val format = if (collection.resourceContainer == rcTarget) "audio/wav" else "text/usfm"

                val relay = BehaviorRelay.create<List<Content>>()
                when (collection.slug.count { it == '_' }) {
                    1 -> {
                        val content = Content(
                            id = autoincrement,
                            start = 1,
                            end = 1,
                            sort = 1,
                            labelKey = ContentLabel.VERSE.value,
                            type = ContentType.TEXT,
                            format = format,
                            text = "/v 1 but test everything; hold fast what is good.",
                            selectedTake = null,
                            draftNumber = 1
                        )
                        relay.accept(listOf(content))
                    }
                    else -> {}
                }
                relay
            }
            whenever(clearContentForCollection(any(), any()))
                .doAnswer {
                    clearContentForCollectionTriggered = true
                    Single.just(emptyList())
                }
            whenever(addContentForCollection(any(), any())).thenReturn(Completable.complete())
        }

        rcSource = rcBase.copy(id = autoincrement, language = english, path = projectDir.root)
        rcTarget = rcBase.copy(id = autoincrement, language = spanish)

        collSource = collectionBase.copy(resourceContainer = rcSource, id = autoincrement)
        collTarget = collectionBase.copy(resourceContainer = rcTarget, id = autoincrement)

        dublinCore = getDublinCore(rcSource)

        sourceAudioDir = projectDir.newFolder(*RcConstants.SOURCE_AUDIO_DIR.split("/").toTypedArray())

        mockedDirectoryProvider.apply {
            whenever(getProjectDirectory(any(), any(), any() as Collection)).thenReturn(projectDir.root)
            whenever(getProjectSourceAudioDirectory(any(), any(), any())).thenReturn(sourceAudioDir)
        }

        projectFilesAccessor = ProjectFilesAccessor(mockedDirectoryProvider, rcSource, rcTarget, collTarget)
        audioSourceAudioAccessor = SourceAudioAccessor(mockedDirectoryProvider, rcSource, collSource.slug)

        workbook = buildWorkbook(mockedDirectoryProvider, mockedDb, collSource, collTarget)
        chapter = workbook.target.chapters.blockingFirst()

        rc = createRcWithAudio()
    }

    @Test
    fun sourceFilesDeleted() {
        Assert.assertEquals(projectFilesAccessor.sourceAudioDir.listFiles()?.size ?: 0, 2)
        ResetChunks().resetChapter(projectFilesAccessor, chapter)
        Assert.assertEquals(projectFilesAccessor.sourceAudioDir.listFiles()?.size ?: 0, 0)
    }

    @Test
    fun clearContentForCollectionTriggered() {
        ResetChunks().resetChapter(projectFilesAccessor, chapter)
        Assert.assertEquals(true, clearContentForCollectionTriggered)

        chapter.chunks.take(1).blockingFirst().forEach {
            Assert.assertEquals(-1, it.draftNumber)
        }
    }

    @Test
    fun takesMarkedForDeletion() {
        val takes = chapter.chunks.take(1).blockingFirst().map { chunk ->
            chunk.audio.getAllTakes().filter { it.deletedTimestamp.value?.value == null }
        }
        Assert.assertEquals(1, takes.size)

        ResetChunks().resetChapter(projectFilesAccessor, chapter)

        val deletedTakes = chapter.chunks.take(1).blockingFirst().map { chunk ->
            chunk.audio.getAllTakes().filter { it.deletedTimestamp.value?.value != null }
        }
        Assert.assertEquals(1, deletedTakes.size)
    }

    private fun createRcWithAudio(): ResourceContainer {
        val fileName = templateAudioFileName(
            rcSource.language.slug, rcSource.identifier, collSource.slug, "{chapter}"
        )
        val sourceFile = createWavFile(tempDir.root, "${fileName.replace("{chapter}", "1")}.wav", "123456".toByteArray())
        val sourceCueFile = File(tempDir.root, "${fileName.replace("{chapter}", "1")}.cue").apply { createNewFile() }

        val audio = OratureAudioFile(sourceFile)
        audio.clearCues()
        audio.update()
        val sourceCues = listOf(
            AudioCue(0, "1"),
            AudioCue(10, "2"),
            AudioCue(20, "3"),
            AudioCue(30, "4"),
            AudioCue(40, "5")
        )
        audio.importCues(sourceCues)
        audio.update()

        return createTestRc(projectDir.root, dublinCore, listOf(sourceFile, sourceCueFile))
    }
}