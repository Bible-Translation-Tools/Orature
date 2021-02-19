package integrationtest.initialization

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeProjects
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.ContainerType
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.data.model.ResourceMetadata
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeProjects {

    @Inject
    lateinit var initProjectsProvider: Provider<InitializeProjects>

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var env: DatabaseEnvironment

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

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

    @Test
    fun testInitializeProjects() {
        prepareInitialProject()

        val testSub = TestObserver<Completable>()
        val init = initProjectsProvider.get()
        init
            .exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, env.db.installedEntityDao.fetchVersion(init))

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
        val targetDir = directoryProvider.getProjectDirectory(
            sourceMetadata,
            targetMetadata,
            project
        )
        env.unzipProject("en-x-demo1-ulb-rev.zip", targetDir)
    }
}
