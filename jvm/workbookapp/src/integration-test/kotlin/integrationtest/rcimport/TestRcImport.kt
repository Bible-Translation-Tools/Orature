package integrationtest.rcimport

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.ContentType.*
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File

class TestRcImport {

    @Test
    fun ulb() {
        ImportEnvironment()
            .import("en_ulb.zip")
            .assertRowCounts(
                Counts(
                    contents = mapOf(
                        TEXT to 31103,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    /**
     * Runs the same test as ulb(), but rather than test the provided and tested ulb resource container,
     * we instead test the version downloaded from WACS through the downloadUlb gradle task. Failure of this
     * test while succeeding the ulb() test therefore implies either potential issues in the WACS repository
     * or a failure to download the content.
     */
    @Test
    fun ulbFromWacs() {
        ImportEnvironment()
            .import("en_ulb.zip", true)
            .assertRowCounts(
                Counts(
                    contents = mapOf(
                        TEXT to 31104,
                        META to 1189
                    ),
                    collections = 1256,
                    links = 0
                )
            )
    }

    @Test
    fun ulbAndTn() {
        ImportEnvironment()
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .assertRowCounts(
                Counts(
                    contents = mapOf(
                        META to 1189,
                        TEXT to 31103,
                        TITLE to 80148,
                        BODY to 77433
                    ),
                    collections = 1256,
                    links = 157573
                )
            )
    }

    @Test
    fun obsV6() {
        ImportEnvironment()
            .import("obs-biel-v6.zip")
            .assertRowCounts(
                Counts(
                    collections = 57,
                    contents = mapOf(
                        META to 55,
                        TEXT to 1314
                    ),
                    links = 0
                )
            )
    }

    @Test
    fun obsAndTnV6() {
        ImportEnvironment()
            .import("obs-biel-v6.zip")
            .import("obs-tn-biel-v6.zip")
            .assertRowCounts(
                Counts(
                    contents = mapOf(
                        META to 55,
                        TEXT to 1314,
                        TITLE to 2237,
                        BODY to 2237
                    ),
                    collections = 57,
                    links = 4474
                )
            )
    }

    @Test
    fun obsSlugs() {
        ImportEnvironment()
            .import("obs-biel-v6.zip")
            .assertSlugs(
                "obs",
                CollectionDescriptor(label = "book", slug = "obs"),
                CollectionDescriptor(label = "project", slug = "obs"),
                CollectionDescriptor(label = "chapter", slug = "obs_1")
            )
    }

    @Test
    fun ulbSlugs() {
        ImportEnvironment()
            .import("en_ulb.zip")
            .assertSlugs(
                "ulb",
                CollectionDescriptor(label = "bundle", slug = "ulb"),
                CollectionDescriptor(label = "project", slug = "gen"),
                CollectionDescriptor(label = "chapter", slug = "gen_1")
            )
    }
}

private class ImportEnvironment {
    val persistenceComponent: TestPersistenceComponent =
        DaggerTestPersistenceComponent
            .builder()
            .testDirectoryProviderModule(TestDirectoryProviderModule())
            .build()
    val db: AppDatabase = persistenceComponent.injectDatabase()
    val injector = Injector(persistenceComponent = persistenceComponent)

    init {
        setUpDatabase()
    }

    val importer
        get() = ImportResourceContainer(
            injector.resourceContainerRepository,
            injector.directoryProvider,
            injector.zipEntryTreeBuilder
        )

    fun import(rcFile: String, importAsStream: Boolean = false): ImportEnvironment {
        val result = if (importAsStream) {
            importer.import(rcFile, rcResourceStream(rcFile)).blockingGet()
        } else {
            importer.import(rcResourceFile(rcFile)).blockingGet()
        }
        Assert.assertEquals(ImportResult.SUCCESS, result)
        return this
    }

    fun assertRowCounts(expected: Counts): ImportEnvironment {
        val contentsByType = db.contentDao.fetchAll()
            .groupBy { it.type_fk }
            .mapValues { it.value.count() }
            .mapKeys { db.contentTypeDao.fetchForId(it.key)!! }
        Assert.assertEquals(
            expected,
            Counts(
                collections = db.collectionDao.fetchAll().count(),
                contents = contentsByType,
                links = db.resourceLinkDao.fetchAll().count()
            )
        )

        return this
    }

    fun assertSlugs(
        rcSlug: String,
        vararg collectionSlug: CollectionDescriptor
    ): ImportEnvironment {
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
        ImportLanguages(langNames, injector.languageRepo)
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

private data class Counts(
    val collections: Int,
    val links: Int,
    val contents: Map<ContentType, Int>
)

private data class CollectionDescriptor(
    val label: String,
    val slug: String
)
