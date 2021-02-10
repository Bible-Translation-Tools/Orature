package integrationtest.initialization

import integrationtest.DaggerTestPersistenceComponent
import integrationtest.TestDirectoryProviderModule
import integrationtest.TestPersistenceComponent
import org.wycliffeassociates.otter.assets.initialization.LANGNAMES_PATH
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import javax.inject.Inject
import javax.inject.Provider

abstract class PersistenceEnvironment {
    protected val persistenceComponent: TestPersistenceComponent =
        DaggerTestPersistenceComponent
            .builder()
            .testDirectoryProviderModule(TestDirectoryProviderModule())
            .build()
    val db: AppDatabase = persistenceComponent.injectDatabase()

    @Inject
    lateinit var importLanguagesProvider: Provider<ImportLanguages>

    init {
        DaggerTestPersistenceComponent.create().inject(this)
        setUpDatabase()
    }

    protected abstract fun setUpDatabase()

    protected fun initLanguages() {
        val langNames = ClassLoader.getSystemResourceAsStream(LANGNAMES_PATH)!!
        importLanguagesProvider
            .get()
            .import(langNames)
            .onErrorComplete()
            .blockingAwait()
    }
}
