package integrationtest.rcimport

import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.ContentType
import org.wycliffeassociates.otter.common.data.model.ContentType.*
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.persistence.database.AppDatabase
import java.io.File

class TestRcImport {

    @Test
    fun ulb() {
        val env = ImportEnvironment()
        env.import("en_ulb.zip")

        env.assertRowCounts(
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

    @Test
    fun ulbAndTn() {
        val env = ImportEnvironment()
        env.import("en_ulb.zip")
        env.import("en_tn.zip")

        env.assertRowCounts(
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
        val env = ImportEnvironment()
        env.import("obs-biel-v6.zip")

        env.assertRowCounts(
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
        val env = ImportEnvironment()
        env.import("obs-biel-v6.zip")
        env.import("obs-tn-biel-v6.zip")

        env.assertRowCounts(
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

    fun import(rcFile: String) {
        val result = importer.import(rcResourceFile(rcFile)).blockingGet()
        Assert.assertEquals(ImportResult.SUCCESS, result)
    }

    fun assertRowCounts(expected: Counts) {
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
}

private data class Counts(
    val collections: Int,
    val links: Int,
    val contents: Map<ContentType, Int>
)
