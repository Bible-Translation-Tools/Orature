package integrationtest.initialization

import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeProjects
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import java.io.File
import java.time.LocalDate

class TestInitializeProjects {

    private val sourceMetadata = ResourceMetadata(
        "rc0.2",
        "Door43 World Missions Community",
        "",
        "",
        "ulb",
        LocalDate.now(),
        Language("en", "", "", "", true),
        LocalDate.now(),
        "",
        "",
        ContainerType.Book,
        "",
        "12",
        File(".")
    )

    private val targetMetadata = sourceMetadata.copy(
        creator = "Orature",
        language = Language("en-x-demo1", "", "", "", true)
    )

    private val project = Collection(
        1,
        "rev",
        "rev",
        "",
        null
    )

    private val env = DatabaseEnvironment()
    private val inj = env.injector

    @Test
    fun testInitializeProjects() {
        prepareInitialProject()

        val testSub = TestObserver<Completable>()
        val init = InitializeProjects(
            inj.resourceMetadataRepository,
            inj.resourceContainerRepository,
            inj.collectionRepo,
            inj.contentRepository,
            inj.takeRepository,
            inj.languageRepo,
            inj.directoryProvider,
            inj.zipEntryTreeBuilder,
            inj.installedEntityRepository
        )
        init
            .exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        env.assertRowCounts(
            RowCount(
                contents = mapOf(
                    ContentType.META to 1211,
                    ContentType.TEXT to 31509
                ),
                collections = 1279,
                links = 0
            )
        )
    }

    private fun prepareInitialProject() {
        val targetDir = inj.directoryProvider.getProjectDirectory(
            sourceMetadata,
            targetMetadata,
            project
        )
        env.unzipProject("en-x-demo1-ulb-rev.zip", targetDir)
    }
}
