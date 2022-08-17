package org.wycliffeassociates.otter.common.domain.chunking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.*
import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import java.io.File

class ChunkAudioUseCaseTest {

    private var autoincrement: Int = 1
        get() = field++

    private val english = getEnglishLanguage(autoincrement)
    private val spanish = getSpanishLanguage(autoincrement)

    private val rcBase = getResourceMetadata(english)
    private val rcSource = rcBase.copy(id = autoincrement, language = english)
    private val rcTarget = rcBase.copy(id = autoincrement, language = spanish)

    private val collectionBase = getGenesisCollection()
    private val collSource = collectionBase.copy(resourceContainer = rcSource, id = autoincrement)
    private val collTarget = collectionBase.copy(resourceContainer = rcTarget, id = autoincrement)

    private val mockedDirectoryProvider = mock<IDirectoryProvider>()
    private val mockedDb = mock<WorkbookRepository.IDatabaseAccessors>().apply {
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
    }

    private val workbook = buildWorkbook(mockedDb, collSource, collTarget)
    private val cues = listOf(
        AudioCue(0, "1"),
        AudioCue(1, "2"),
        AudioCue(2, "3")
    )

    private val dublinCore = getDublinCore(rcSource)

    @JvmField @Rule val tempDir = TemporaryFolder()
    @JvmField @Rule val directoryProviderTempDir = TemporaryFolder()
    @JvmField @Rule val projectDir = TemporaryFolder()

    @Test
    fun chunkAudioFilesCreated() {
        val sourceFile = createWavFile(tempDir.root, "source.wav", "123456".toByteArray())
        mockedDirectoryProvider.apply {
            whenever(this.tempDirectory).thenReturn(directoryProviderTempDir.root)
            whenever(this.getProjectDirectory(rcSource, rcTarget, workbook.target.toCollection())).thenReturn(projectDir.root)
        }

        val rc = ResourceContainer.create(projectDir.root) {
            manifest = Manifest(dublinCore, listOf(), Checking())
            write()
        }

        ChunkAudioUseCase(mockedDirectoryProvider, workbook)
            .createChunkedSourceAudio(sourceFile, cues)

        Assert.assertEquals(
            rc.accessor.fileExists(".apps/orature/source/audio/${sourceFile.name}"),
            true
        )
        Assert.assertEquals(
            rc.accessor.fileExists(".apps/orature/source/audio/${sourceFile.nameWithoutExtension}.cue"),
            true
        )

        File(projectDir.root, ".apps/orature/source/audio/${sourceFile.name}").apply {
            val audioFile = AudioFile(this)
            val text = readTextFromAudioFile(audioFile, 6)

            Assert.assertEquals(text, "123456")
            Assert.assertEquals(audioFile.metadata.getCues(), cues)
        }
    }
}