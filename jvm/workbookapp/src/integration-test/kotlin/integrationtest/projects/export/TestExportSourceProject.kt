package integrationtest.projects.export

import integrationtest.createTestWavFile
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.enUlbTestMetadata
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.SourceProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestExportSourceProject {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    @Inject
    lateinit var workbookRepository: IWorkbookRepository
    @Inject
    lateinit var exportSourceProvider: Provider<SourceProjectExporter>
    @Inject
    lateinit var importRcProvider: Provider<ImportResourceContainer>

    private val importer
        get() = importRcProvider.get()

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    private val db: DatabaseEnvironment
        get() = dbEnvProvider.get()

    private val sourceMetadata = enUlbTestMetadata
    private val targetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe")
    )
    private val targetCollection = Collection(
        1,
        "rev",
        "rev",
        "",
        targetMetadata
    )
    private lateinit var projectFilesAccessor: ProjectFilesAccessor

    @Before
    fun setUp() {
        db.import("en-x-demo1-ulb-rev.zip")
        workbook = workbookRepository.getProjects().blockingGet().single()
        projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            targetMetadata,
            targetCollection
        )
        outputDir = createTempDirectory("orature-export-test").toFile()
    }

    @Test
    fun exportAsSource() {
        prepareTakeForExport()

        val result = exportSourceProvider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        ResourceContainer.load(outputDir.listFiles().first()).use { rc ->
            assertEquals(1, rc.media?.projects?.size ?: 0)

            val files = getExportedFiles(rc)
            assertEquals(2, files.size)
            assertTrue(files.any { it.endsWith(".mp3") })
            assertTrue(files.any { it.endsWith(".cue") })
        }
    }

    @Test
    fun `export source project has no media when no take selected`() {
        val result = exportSourceProvider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)
        ResourceContainer.load(outputDir.listFiles().first()).use { rc ->
            val filePaths = getExportedFiles(rc)
            assertEquals(0, filePaths.size)
        }
    }

    @Test
    fun exportSourceProjectThenImport() {
        // export as source
        prepareTakeForExport()
        val result = exportSourceProvider.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        // import source from previously exported step
        val importResult = importer
            .import(outputDir.listFiles().first())
            .blockingGet()

        assertEquals(ImportResult.SUCCESS, importResult)

        db.assertRowCounts(
            RowCount(
                contents = mapOf(
                    ContentType.META to 2400,
                    ContentType.TEXT to 62613
                ),
                collections = 2535,
                links = 0
            )
        )
    }

    private fun getExportedFiles(rc: ResourceContainer): Set<String> {
        val projectMediaPath = rc.media?.projects?.first()?.media?.first { it.identifier == "mp3" }?.chapterUrl
        val filePathInContainer = File(projectMediaPath).parentFile.invariantSeparatorsPath
        val files = rc.accessor.getInputStreams(filePathInContainer, listOf("mp3", "cue"))
        files.forEach { (name, ins) ->
            ins.close()
        }
        return files.keys
    }

    private fun prepareTakeForExport() {
        val testTake = createTestWavFile(directoryProvider.tempDirectory)
        val take = Take(
            "chapter-take",
            testTake,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
        // select a take to be included when export
        workbook.target.chapters.blockingFirst().audio.selectTake(take)
        AudioFile(testTake).apply {
            metadata.addCue(1, "marker-1")
            update()
        }
    }
}