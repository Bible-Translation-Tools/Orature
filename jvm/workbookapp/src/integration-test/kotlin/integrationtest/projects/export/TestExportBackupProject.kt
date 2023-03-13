package integrationtest.projects.export

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.enUlbTestMetadata
import integrationtest.projects.DatabaseEnvironment
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.audio.AudioFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.ProjectMetadata
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportOptions
import org.wycliffeassociates.otter.common.domain.project.exporter.ExportResult
import org.wycliffeassociates.otter.common.domain.project.exporter.resourcecontainer.BackupProjectExporter
import org.wycliffeassociates.otter.common.domain.project.takeFilenamePattern
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.mapEach
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestExportBackupProject {

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    @Inject
    lateinit var importer: Provider<ImportProjectUseCase>

    @Inject
    lateinit var exportBackupUseCase: Provider<BackupProjectExporter>

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get() // bootstrap the db
    private val takesPerChapter = 2
    private val contributors = listOf("user1", "user2")
    private val seedProject = buildProjectFile()
    private lateinit var workbook: Workbook
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        importer.get().import(seedProject).blockingGet()
        workbook = workbookRepository.getProjects().blockingGet().single()
        outputDir = createTempDirectory("orature-export-test").toFile()
    }

    @After
    fun cleanUp() {
        outputDir.deleteRecursively()
    }

    @Test
    fun exportProjectWithContributorInfo() {
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                ProjectMetadata(enUlbTestMetadata),
                workbook,
                null
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val exportedContributorList = ResourceContainer.load(file!!).use {
            it.manifest.dublinCore.contributor.toList()
        }
        Assert.assertEquals(contributors, exportedContributorList)
    }

    @Test
    fun exportProjectWithChapterFilter() {
        val chapterFilter = ExportOptions(chapters = listOf(1, 3))
        val result = exportBackupUseCase.get()
            .export(
                outputDir,
                ProjectMetadata(enUlbTestMetadata),
                workbook,
                chapterFilter
            )
            .blockingGet()

        Assert.assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        Assert.assertNotNull(file)

        val chapterToTakes = getTakesByChapterFromProject(file!!)

        Assert.assertEquals(
            chapterFilter.chapters,
            chapterToTakes.keys.toList()
        )
        Assert.assertEquals(
            takesPerChapter * chapterFilter.chapters.size,
            chapterToTakes.values.sum()
        )
    }

    private fun buildProjectFile(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .setContributors(contributors)
            .addTake(1, ContentType.META, 1, true)
            .addTake(2, ContentType.META, 1, true)
            .addTake(3, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.TEXT, 1, true, chapter = 2, start = 1, end = 1)
            .addTake(3, ContentType.TEXT, 1, true, chapter = 3, start = 1, end = 1)
            .buildFile()
    }

    private fun getTakesByChapterFromProject(file: File): Map<Int, Int> {
        val chapterToTakeCount = mutableMapOf<Int, Int>()

        val parseChapter: (String) -> Int = { name: String ->
            takeFilenamePattern
                .matcher(name)
                .apply { find() }
                .group(1)
                .toInt()
        }

        ResourceContainer.load(file).use { rc ->
            val extensionFilter = AudioFileFormat.values().map { it.extension }
            val fileStreamMap = rc.accessor.getInputStreams(".", extensionFilter)
            try {
                fileStreamMap.keys.forEach { name ->
                    val chapterNumber = parseChapter(name)
                    chapterToTakeCount[chapterNumber] = 1 + chapterToTakeCount.getOrPut(chapterNumber) { 0 }
                }
            } finally {
                fileStreamMap.values.forEach { it.close() }
            }
        }

        return chapterToTakeCount
    }
}
