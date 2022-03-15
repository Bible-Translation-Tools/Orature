package integrationtest.projects

import integrationtest.di.DaggerTestPersistenceComponent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Language
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider
import kotlin.io.path.createTempDirectory

class TestProjectExport {
    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject lateinit var exportUseCase: Provider<ProjectExporter>
    @Inject lateinit var workbookRepository: IWorkbookRepository
    @Inject lateinit var directoryProvider: IDirectoryProvider

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val logger = LoggerFactory.getLogger(javaClass)
    private val db = dbEnvProvider.get()

    private val sourceMetadata = ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true, ""),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        "",
        File(".")
    )

    private val targetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true, "Europe")
    )

    private val sourceCollection = Collection(
        1,
        "rev",
        "rev",
        "",
        sourceMetadata
    )

    private val targetCollection = Collection(
        1,
        "rev",
        "rev",
        "",
        targetMetadata
    )

    @Test
    fun exportProjectWithContributors() {
        db.import("en-x-demo1-ulb-rev.zip")
        val workbook = workbookRepository.getProjects().blockingGet().single()
        val projectFilesAccessor = ProjectFilesAccessor(
            directoryProvider,
            sourceMetadata,
            targetMetadata,
            targetCollection
        )
        val outputDir = createTempDirectory("orature-export").toFile()
        outputDir.deleteOnExit()

        val result = exportUseCase.get()
            .export(outputDir, targetMetadata, workbook, projectFilesAccessor)
            .blockingGet()

        assertEquals(ExportResult.SUCCESS, result)

        val file = outputDir.listFiles().singleOrNull()

        assertNotNull(file)

        val exportedContributorList = ResourceContainer.load(file!!).use {
            it.manifest.dublinCore.contributor.toList()
        }
        assertTrue(exportedContributorList.isNotEmpty())
    }
}