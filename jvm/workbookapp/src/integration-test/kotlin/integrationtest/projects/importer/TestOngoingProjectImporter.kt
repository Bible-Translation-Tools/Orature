package integrationtest.projects.importer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.NewSourceImporter
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.domain.project.importer.RCImporter
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestOngoingProjectImporter {
    @Inject
    lateinit var importerProvider: Provider<OngoingProjectImporter>

    @Inject
    lateinit var sourceImporterProvider: Provider<NewSourceImporter>

    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()
    private val importer: RCImporter by lazy {
        val imp = importerProvider.get()
        imp.setNext(sourceImporterProvider.get())
        imp
    }
    private val takesInProject = 6
    private val takesPerChapter = 2
    private val chaptersSelected = listOf(1, 2)
    private val takesAdded = takesPerChapter * chaptersSelected.size

    private val callback = mock<ProjectImporterCallback> {
        on { onRequestUserInput(any()) } doReturn (Single.just(ImportOptions(chaptersSelected)))
    }

    @Test
    fun testImportDuplicatedProject() {
        importOngoingProject(callback = null)
        val originalTakes = db.db.takeDao.fetchAll()

        Assert.assertEquals(takesInProject, originalTakes.size)

        /* Import the same project with chapter selection provided from callback */
        importOngoingProject(callback = this.callback)

        val currentTakes = db.db.takeDao.fetchAll()
        val addedTakes = currentTakes.filter { it !in originalTakes }
        val contents = addedTakes.map { db.db.contentDao.fetchById(it.contentFk) }
        val chapterCollections = contents.map { db.db.collectionDao.fetchById(it.collectionFk) }
        val chaptersImported = chapterCollections
            .map { it.sort }
            .sorted()
            .distinct()

        Assert.assertEquals(takesInProject + takesAdded, currentTakes.size)
        Assert.assertEquals(takesAdded, addedTakes.size)
        Assert.assertEquals(chaptersSelected, chaptersImported)
    }

    private fun importOngoingProject(callback: ProjectImporterCallback?) {
        val projectFile = getProjectFileWith3Chapters()

        importer
            .import(projectFile, callback)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }

        db.assertRowCounts(
            RowCount(
                collections = 1278,
                contents = mapOf(
                    ContentType.META to 1210,
                    ContentType.TEXT to 31214
                ),
                links = 0
            )
        )
    }

    /**
     * There are 3 chapters having audio. Each includes one chapter take and one verse take.
     */
    private fun getProjectFileWith3Chapters(): File {
        val path = javaClass.classLoader.getResource(
            "resource-containers/john-3-chapters-translation.orature"
        ).file
        return File(path)
    }
}