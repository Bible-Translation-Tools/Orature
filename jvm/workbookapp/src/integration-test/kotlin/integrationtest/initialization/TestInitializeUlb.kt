package integrationtest.initialization

import integrationtest.DaggerTestPersistenceComponent
import integrationtest.TestDirectoryProviderModule
import integrationtest.TestPersistenceComponent
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeUlb
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeUlb {

    @Inject
    lateinit var initUlbProvider: Provider<InitializeUlb>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
    }

    @Test
    fun testImportEnUlb() {
        val testSub = TestObserver<Completable>()
        val env = InitializeUlbEnvironment()

        val init = initUlbProvider.get()
        init.exec()
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, env.db.installedEntityDao.fetchVersion(init))
    }
}

private class InitializeUlbEnvironment : PersistenceEnvironment() {
    override fun setUpDatabase() {
        initLanguages()
    }
}
