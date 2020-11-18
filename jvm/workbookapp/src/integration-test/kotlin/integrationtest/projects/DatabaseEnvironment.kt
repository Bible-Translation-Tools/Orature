package integrationtest.projects

import integrationtest.DaggerTestPersistenceComponent
import integrationtest.TestDirectoryProviderModule
import integrationtest.TestPersistenceComponent
import jooq.Tables.CONTENT_DERIVATIVE
import org.junit.Assert
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Language
import org.wycliffeassociates.otter.common.domain.collections.CreateProject
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.utils.ZipUtils
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import java.io.File

class DatabaseEnvironment {
    private val persistenceComponent: TestPersistenceComponent =
        DaggerTestPersistenceComponent
            .builder()
            .testDirectoryProviderModule(TestDirectoryProviderModule())
            .build()

    val db: AppDatabase = persistenceComponent.injectDatabase()
    val injector =
        Injector(persistenceComponent = persistenceComponent)

    init {
        setUpDatabase()
    }

    private val importer
        get() = ImportResourceContainer(
            injector.resourceMetadataRepository,
            injector.resourceContainerRepository,
            injector.collectionRepo,
            injector.contentRepository,
            injector.takeRepository,
            injector.languageRepo,
            injector.directoryProvider,
            injector.zipEntryTreeBuilder
        )

    fun import(rcFile: String, importAsStream: Boolean = false, unzip: Boolean = false): DatabaseEnvironment {
        val result = if (importAsStream) {
            importer.import(rcFile, rcResourceStream(rcFile)).blockingGet()
        } else {
            val resourceFile = if (unzip) {
                unzipProject(rcFile)
            } else {
                rcResourceFile(rcFile)
            }
            importer.import(resourceFile).blockingGet()
        }
        Assert.assertEquals(
            ImportResult.SUCCESS,
            result
        )
        return this
    }

    fun createProject(sourceProject: Collection, targetLanguage: Language): Collection =
        CreateProject(injector.collectionRepo, injector.resourceMetadataRepository)
            .create(sourceProject, targetLanguage)
            .blockingGet()

    fun unzipProject(rcFile: String, dir: File? = null): File {
        return ZipUtils.unzip(rcResourceFile(rcFile), dir)
    }

    fun assertRowCounts(expected: RowCount, message: String? = null): DatabaseEnvironment {
        val actual = RowCount(
            // These ?.let constructs let us skip comparing counts that aren't specified in [expected].
            contents = expected.contents?.let { _ -> fetchContentRowCount() },
            collections = expected.collections?.let { _ -> fetchCollectionRowCount() },
            links = expected.links?.let { _ -> fetchLinkRowCount() },
            derivatives = expected.derivatives?.let { _ -> fetchDerivativeRowCount() }
        )
        Assert.assertEquals(message, expected, actual)
        return this
    }

    fun assertSlugs(
        rcSlug: String,
        vararg collectionSlug: CollectionDescriptor
    ): DatabaseEnvironment {
        val rc = db.resourceMetadataDao.fetchAll().firstOrNull { it.identifier == rcSlug }
        Assert.assertNotNull("Retrieving resource container info", rc)

        collectionSlug.forEach { (label, slug) ->
            val entity = db.collectionDao.fetch(containerId = rc!!.id, label = label, slug = slug)
            Assert.assertNotNull("Retrieving $label $slug", entity)
        }

        return this
    }

    private fun setUpDatabase() {
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        ImportLanguages(
            langNames,
            injector.languageRepo
        )
            .import()
            .onErrorComplete()
            .blockingAwait()
    }

    private fun rcResourceFile(rcFile: String) =
        File(
            TestRcImport::class.java.classLoader
                .getResource("resource-containers/$rcFile")!!
                .toURI()
                .path
        )

    /**
     * The path here should match that of the resource structure of main
     */
    private fun rcResourceStream(rcFile: String) =
        TestRcImport::class.java.classLoader
            .getResourceAsStream("content/$rcFile")!!

    private fun fetchCollectionRowCount() = db.collectionDao.fetchAll().count()
    private fun fetchLinkRowCount() = db.resourceLinkDao.fetchAll().count()
    private fun fetchDerivativeRowCount() = db.dsl.selectCount().from(CONTENT_DERIVATIVE).fetchOne(0) as Int
    private fun fetchContentRowCount() =
        db.contentDao.fetchAll()
            .groupBy { it.type_fk }
            .mapValues { it.value.count() }
            .mapKeys { db.contentTypeDao.fetchForId(it.key)!! }
}

data class CollectionDescriptor(
    val label: String,
    val slug: String
)
