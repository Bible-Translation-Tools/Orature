package integrationtest.initialization

import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeUlb

class TestInitializeUlb {

    @Test
    fun testImportEnUlb() {
        val testSub = TestObserver<Completable>()
        val env = InitializeUlbEnvironment()
        val inj = env.injector

        val init = InitializeUlb(
            inj.installedEntityRepository,
            inj.resourceRepository,
            inj.resourceContainerRepository,
            inj.collectionRepo,
            inj.contentRepository,
            inj.takeRepository,
            inj.languageRepo,
            inj.directoryProvider,
            inj.zipEntryTreeBuilder
        )
        init
            .exec()
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