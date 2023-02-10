package integrationtest.persistence.repository

import integrationtest.di.DaggerTestPersistenceComponent
import integrationtest.projects.DatabaseEnvironment
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import javax.inject.Inject
import javax.inject.Provider

class TestResourceContainerRepository {
    @Inject
    lateinit var dbEnvProvider: Provider<DatabaseEnvironment>
    @Inject
    lateinit var directoryProvider: IDirectoryProvider
    @Inject
    lateinit var collectionRepository: ICollectionRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    private val db = dbEnvProvider.get()

    @Test
    fun testUpdate() {
        db.import("en_ulb.zip")


    }
}