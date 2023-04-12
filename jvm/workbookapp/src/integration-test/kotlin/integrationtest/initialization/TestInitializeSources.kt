package integrationtest.initialization

import integrationtest.di.DaggerTestPersistenceComponent
import io.reactivex.Completable
import io.reactivex.observers.TestObserver
import io.reactivex.subjects.PublishSubject
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.assets.initialization.InitializeSources
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class TestInitializeSources {
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var directoryProvider: IDirectoryProvider

    @Inject
    lateinit var initSourcesProvider: Provider<InitializeSources>

    @Inject
    lateinit var importLanguages: Provider<ImportLanguages>

    @Inject
    lateinit var resourceMetadataRepository: IResourceMetadataRepository

    init {
        DaggerTestPersistenceComponent.create().inject(this)
        val langNames = ClassLoader.getSystemResourceAsStream("content/langnames.json")!!
        importLanguages.get().import(langNames).blockingGet()
    }

    @Test
    fun testInitializeSources() {
        prepareSource()

        Assert.assertEquals(
            0, resourceMetadataRepository.getAllSources().blockingGet().size
        )

        val testSub = TestObserver<Completable>()
        val init = initSourcesProvider.get()
        val mockProgressEmitter = PublishSubject.create<ProgressStatus>().apply {
            onComplete()
        }

        init
            .exec(mockProgressEmitter)
            .subscribe(testSub)

        testSub.assertComplete()
        testSub.assertNoErrors()

        Assert.assertEquals(init.version, database.installedEntityDao.fetchVersion(init))
        Assert.assertEquals(
            1, resourceMetadataRepository.getAllSources().blockingGet().size
        )
    }

    private fun prepareSource() {
        val sourceToCopy = File(javaClass.classLoader.getResource("resource-containers/hi_ulb.zip").file)
        val targetSource = directoryProvider.internalSourceRCDirectory.resolve(sourceToCopy.name)

        sourceToCopy.copyTo(targetSource, overwrite = true)
    }
}