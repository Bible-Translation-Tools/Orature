package org.wycliffeassociates.otter.common.domain.content

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.*
import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.Content
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.SourceAudioAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.RcConstants
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IVersificationRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDatabaseAccessors
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.*
import java.io.File
import java.time.LocalDate

class CreateChunksTest {
    @JvmField @Rule
    val tempDir = TemporaryFolder()
    @JvmField @Rule
    val providerTempDir = TemporaryFolder()
    @JvmField @Rule
    val projectDir = TemporaryFolder()

    private lateinit var sourceAudioDir: File
    private var chunksAddedToDatabase = false

    private var autoincrement: Int = 1
        get() = field++

    private val english = getEnglishLanguage(autoincrement)
    private val spanish = getSpanishLanguage(autoincrement)

    private val rcBase = getResourceMetadata(english)
    private lateinit var rcSource: ResourceMetadata
    private lateinit var rcTarget: ResourceMetadata

    private val collectionBase = getGenesisCollection()
    private lateinit var collSource: Collection
    private lateinit var collTarget: Collection

    private lateinit var workbook: Workbook
    private lateinit var chapter: Chapter
    private lateinit var rc: ResourceContainer

    private val mockedDirectoryProvider = mock<IDirectoryProvider>()
    private val mockedDb = mock<IWorkbookDatabaseAccessors>()

    private val sourceCues = listOf(
        AudioCue(0, "1"),
        AudioCue(10, "2"),
        AudioCue(20, "3"),
        AudioCue(30, "4"),
        AudioCue(40, "5")
    )

    private val customCues = listOf(
        AudioCue(0, "1"),
        AudioCue(20, "2")
    )

    private lateinit var dublinCore: DublinCore

    private lateinit var projectFilesAccessor: ProjectFilesAccessor
    private lateinit var audioSourceAudioAccessor: SourceAudioAccessor

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
                val take = if (content.format == "audio/wav" && content.start == 3) {
                    val id = autoincrement
                    Take(
                        id = id,
                        number = id,
                        path = File("."),
                        filename = ".",
                        markers = listOf(),
                        played = false,
                        created = LocalDate.now(),
                        deleted = null,
                        checkingStatus = CheckingStatus.UNCHECKED,
                        checksum = null
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
            whenever(addContentForCollection(any(), any()))
                .thenReturn(
                    Completable.fromAction {
                        chunksAddedToDatabase = true
                    }
                )
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

        CreateChunks(mock()).createUserDefinedChunks(workbook, chapter, customCues, 1)
    }

    @After
    fun tearDown() {
        chunksAddedToDatabase = false
    }

    @Test
    fun testChunksAddedToDatabase() {
        Assert.assertEquals(chunksAddedToDatabase, true)
    }

    @Test
    fun testWrittenChunksEqualToUserChunks() {
        val chunks = readChunksFile(projectDir.root)
        Assert.assertEquals(chunks[chapter.sort]?.size, customCues.size)
    }

    @Test
    fun testChunkCreatedInResourceContainer() {
        val rc = createRcWithAudio()
        Assert.assertEquals(rc.accessor.fileExists(RcConstants.CHUNKS_FILE), true)
    }

    private fun createRcWithAudio(): ResourceContainer {
        val fileName = templateAudioFileName(
            rcSource.language.slug, rcSource.identifier, collSource.slug, "{chapter}"
        )
        val sourceFile = createWavFile(tempDir.root, "${fileName.replace("{chapter}", "1")}.wav", "123456".toByteArray())
        val sourceCueFile = File(tempDir.root, "${fileName.replace("{chapter}", "1")}.cue").apply { createNewFile() }

        val audio = OratureAudioFile(sourceFile)
        audio.metadata.clearMarkers()
        audio.update()
        for (cue in sourceCues) {
            audio.metadata.addCue(cue.location, cue.label)
        }
        audio.update()

        return createTestRc(projectDir.root, dublinCore, listOf(sourceFile, sourceCueFile))
    }
}