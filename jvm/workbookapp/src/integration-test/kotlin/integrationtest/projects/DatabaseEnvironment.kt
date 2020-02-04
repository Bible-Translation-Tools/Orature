package integrationtest.projects

import integrationtest.DaggerTestPersistenceComponent
import integrationtest.TestDirectoryProviderModule
import integrationtest.TestPersistenceComponent
import jooq.Tables
import org.junit.Assert
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import java.io.File

class DatabaseEnvironment {
    private val persistenceComponent: TestPersistenceComponent =
        DaggerTestPersistenceComponent
            .builder()
            .testDirectoryProviderModule(TestDirectoryProviderModule())
            .build()
    private val db: AppDatabase = persistenceComponent.injectDatabase()
    private val injector =
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

    fun import(rcFile: String, importAsStream: Boolean = false): DatabaseEnvironment {
        val result = if (importAsStream) {
            importer.import(rcFile, rcResourceStream(rcFile)).blockingGet()
        } else {
            importer.import(rcResourceFile(rcFile)).blockingGet()
        }
        Assert.assertEquals(
            ImportResult.SUCCESS,
            result
        )
        return this
    }

    fun assertRowCounts(expected: RowCount, message: String? = null): DatabaseEnvironment {
        val contentsByType = db.contentDao.fetchAll()
            .groupBy { it.type_fk }
            .mapValues { it.value.count() }
            .mapKeys { db.contentTypeDao.fetchForId(it.key)!! }
        Assert.assertEquals(
            message,
            expected,
            RowCount(
                collections = db.collectionDao.fetchAll().count(),
                contents = contentsByType,
                links = db.resourceLinkDao.fetchAll().count(),
                derivatives = db.dsl.selectCount().from(Tables.CONTENT_DERIVATIVE).fetchOne(0) as Int
            )
        )

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
}

data class CollectionDescriptor(
    val label: String,
    val slug: String
)