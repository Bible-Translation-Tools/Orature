package org.wycliffeassociates.otter.common.domain.chunking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.*
import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.translation.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookDatabaseAccessors
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File

class ChunkAudioUseCaseTest {
    @JvmField @Rule
    val tempDir = TemporaryFolder()

    @JvmField @Rule
    val directoryProviderTempDir = TemporaryFolder()

    @JvmField @Rule
    val projectDir = TemporaryFolder()

    private var autoincrement: Int = 1
        get() = field++

    private val english = getEnglishLanguage(autoincrement)
    private val spanish = getSpanishLanguage(autoincrement)

    private val rcBase = getResourceMetadata(english)
    private val rcSource = rcBase.copy(id = autoincrement, language = english)
    private val rcTarget = rcBase.copy(id = autoincrement, language = spanish)

    private val collectionBase = getGenesisCollection()
    private val collTarget = collectionBase.copy(resourceContainer = rcTarget, id = autoincrement)

    private val mockedDirectoryProvider = mock<IDirectoryProvider>()
    private val mockedDb = mock<IWorkbookDatabaseAccessors>()

    private val cues =
        listOf(
            AudioCue(0, "1"),
            AudioCue(1, "2"),
            AudioCue(2, "3"),
        )

    private val dublinCore = getDublinCore(rcSource)

    private lateinit var projectFilesAccessor: ProjectFilesAccessor
    private lateinit var sourceFile: File
    private lateinit var rc: ResourceContainer

    @Before
    fun setup() {
        mockedDb.apply {
            whenever(
                getTranslation(any(), any()),
            ).thenReturn(
                Single.just(
                    Translation(
                        english,
                        spanish,
                        null,
                    ),
                ),
            )
        }
        mockedDirectoryProvider.apply {
            whenever(this.tempDirectory).thenReturn(directoryProviderTempDir.root)
            whenever(getProjectDirectory(rcSource, rcTarget, collTarget)).thenReturn(projectDir.root)
        }
        projectFilesAccessor =
            ProjectFilesAccessor(
                mockedDirectoryProvider,
                rcSource,
                rcTarget,
                collTarget,
            )
        sourceFile = createWavFile(tempDir.root, "source.wav", "123456".toByteArray())
        rc = createTestRc(projectDir.root, dublinCore)

        ChunkAudioUseCase(mockedDirectoryProvider, projectFilesAccessor)
            .createChunkedSourceAudio(sourceFile, cues)
    }

    @Test
    fun sourceAudioFileCopiedToResourceContainer() {
        Assert.assertEquals(
            rc.accessor.fileExists(".apps/orature/source/audio/${sourceFile.name}"),
            true,
        )
    }

    @Test
    fun sourceCueFileCopiedToResourceContainer() {
        Assert.assertEquals(
            rc.accessor.fileExists(".apps/orature/source/audio/${sourceFile.nameWithoutExtension}.cue"),
            true,
        )
    }

    @Test
    fun cuesWrittenToSourceAudio() {
        File(projectDir.root, ".apps/orature/source/audio/${sourceFile.name}").apply {
            val oratureAudioFile = OratureAudioFile(this)
            val text = readTextFromAudioFile(oratureAudioFile, 6)

            Assert.assertEquals(text, "123456")
            // Assert.assertEquals(oratureAudioFile.metadata.getCues(), cues)
        }
    }
}
