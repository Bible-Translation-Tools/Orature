package org.wycliffeassociates.otter.common.domain.chunking

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.data.primitives.*
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import org.wycliffeassociates.resourcecontainer.entity.Checking
import org.wycliffeassociates.resourcecontainer.entity.Manifest
import org.wycliffeassociates.resourcecontainer.entity.dublincore
import org.wycliffeassociates.resourcecontainer.entity.language
import java.io.File
import java.time.LocalDate

class ChunkAudioUseCaseTest {

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
    private val spanish = Language(
        "es",
        "Spanish",
        "Spanish",
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
        license = "",
        path = File(".")
    )
    private val rcSource = rcBase.copy(id = autoincrement, language = english)
    private val rcTarget = rcBase.copy(id = autoincrement, language = spanish)

    private val collectionBase = Collection(
        sort = 1,
        slug = "gen",
        labelKey = "project",
        titleKey = "Genesis",
        resourceContainer = null
    )
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
    private val workbook = buildWorkbook(mockedDb)
    private val cues = listOf(
        AudioCue(0, "1"),
        AudioCue(1, "2"),
        AudioCue(2, "3")
    )

    private val dublinCore = dublincore {
        conformsTo = "0.2"
        identifier = rcSource.identifier
        issued = LocalDate.now().toString()
        modified = LocalDate.now().toString()
        language = language {
            identifier = rcSource.language.slug
            direction = rcSource.language.direction
            title = rcSource.language.name
        }
        creator = "Orature"
        version = rcSource.version
        rights = rcSource.license
        format = MimeType.of(rcSource.format).norm
        subject = rcSource.subject
        type = rcSource.type.slug
        title = rcSource.title
    }

    @JvmField @Rule val tempDir = TemporaryFolder()
    @JvmField @Rule val projectDir = TemporaryFolder()

    @Test
    fun chunkAudioFilesCreated() {
        val sourceFile = createWavFile("source", "123456".toByteArray())
        mockedDirectoryProvider.apply {
            whenever(this.tempDirectory).thenReturn(tempDir.root)
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
            val reader = audioFile.reader()
            val buffer = ByteArray(6) // to store 6 characters (123456)
            reader.open()
            var outStr = ""
            while (reader.hasRemaining()) {
                reader.getPcmBuffer(buffer)
                outStr = buffer.decodeToString()
            }
            Assert.assertEquals(outStr, "123456")
            Assert.assertEquals(audioFile.metadata.getCues(), cues)
        }
    }

    private fun createWavFile(name: String, data: ByteArray): File {
        val file = File.createTempFile(name, ".wav")
        val audioFile = AudioFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        audioFile.writer().use { os ->
            os.write(data)
        }
        return file
    }

    private fun buildWorkbook(
        db: WorkbookRepository.IDatabaseAccessors,
        source: Collection = collSource,
        target: Collection = collTarget
    ) = WorkbookRepository(
        mock(),
        db
    ).get(source, target)
}