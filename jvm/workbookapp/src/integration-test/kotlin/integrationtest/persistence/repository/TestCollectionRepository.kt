package integrationtest.persistence.repository

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.RowCount
import jooq.Tables.TAKE_ENTITY
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import javax.inject.Inject
import javax.inject.Provider

class TestCollectionRepository {

    @Inject lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var collectionRepository: ICollectionRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()

    @Test
    fun testDeleteResource() {
        db
            .import("en_ulb.zip")
            .import("en_tn.zip")
            .import("en-x-demo1-tn-rev.zip")
            .assertRowCounts(
                RowCount(
                    contents = mapOf(
                        ContentType.META to 1211,
                        ContentType.TEXT to 31104,
                        ContentType.TITLE to 81419,
                        ContentType.BODY to 78637
                    ),
                    collections = 1279,
                    links = 157581
                )
            )

        val dsl = db.db.dsl
        var takeCount = dsl
            .select(TAKE_ENTITY.asterisk())
            .from(TAKE_ENTITY)
            .count()

        Assert.assertTrue(takeCount > 0)

        val project = collectionRepository
            .getDerivedProjects().blockingGet()
            .single()

        collectionRepository.deleteResources(project, true).blockingAwait()
        takeCount = dsl
            .select(TAKE_ENTITY.asterisk())
            .from(TAKE_ENTITY)
            .count()

        Assert.assertEquals(0, takeCount)
    }
}