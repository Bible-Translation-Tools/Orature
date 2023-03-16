package integrationtest.projects.importer

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import io.reactivex.Single
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.ResourceContainerBuilder
import org.wycliffeassociates.otter.common.data.primitives.ContentType
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
    private val projectFile: File by lazy { setupRC() }
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

        Assert.assertEquals(
            "There should be $takesInProject takes after the initial import.",
            takesInProject,
            originalTakes.size
        )

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

        Assert.assertEquals(
            "There should be $takesInProject + $takesAdded takes " +
                    "after importing chapter: $chaptersSelected",
            takesInProject + takesAdded,
            currentTakes.size
        )
        Assert.assertEquals(takesAdded, addedTakes.size)
        Assert.assertEquals(chaptersSelected, chaptersImported)
    }

    private fun importOngoingProject(callback: ProjectImporterCallback?) {
        importer
            .import(projectFile, callback)
            .blockingGet()
            .let {
                Assert.assertEquals(ImportResult.SUCCESS, it)
            }
    }

    private fun setupRC(): File {
        return ResourceContainerBuilder
            .setUpEmptyProjectBuilder()
            .addTake(1, ContentType.META, 1, true)
            .addTake(2, ContentType.META, 1, true)
            .addTake(3, ContentType.META, 1, true)
            .addTake(1, ContentType.TEXT, 1, true, chapter = 1, start = 1, end = 1)
            .addTake(2, ContentType.TEXT, 1, true, chapter = 2, start = 1, end = 1)
            .addTake(3, ContentType.TEXT, 1, true, chapter = 3, start = 1, end = 1)
            .buildFile()
    }
}