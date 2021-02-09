package integrationtest.initialization

import integrationtest.DaggerTestPersistenceComponent
import integrationtest.TestDirectoryProviderModule
import integrationtest.TestPersistenceComponent
import org.wycliffeassociates.otter.assets.initialization.LANGNAMES_PATH
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.inject.Injector

abstract class PersistenceEnvironment {
    protected val persistenceComponent: TestPersistenceComponent =
        DaggerTestPersistenceComponent
            .builder()
            .testDirectoryProviderModule(TestDirectoryProviderModule())
            .build()
    val db: AppDatabase = persistenceComponent.injectDatabase()
    val injector = Injector(persistenceComponent = persistenceComponent)

    init {
        setUpDatabase()
    }

    protected abstract fun setUpDatabase()

    protected fun initLanguages() {
        val langNames = ClassLoader.getSystemResourceAsStream(LANGNAMES_PATH)!!
        ImportLanguages(langNames, persistenceComponent.languageRepo)
            .import()
            .onErrorComplete()
            .blockingAwait()
    }
}
