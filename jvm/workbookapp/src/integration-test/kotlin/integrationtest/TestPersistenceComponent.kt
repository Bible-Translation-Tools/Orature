package integrationtest

import dagger.Component
import integrationtest.initialization.PersistenceEnvironment
import integrationtest.initialization.TestInitializeProjects
import integrationtest.initialization.TestInitializeUlb
import integrationtest.projects.DatabaseEnvironment
import integrationtest.projects.TestProjectCreate
import org.wycliffeassociates.otter.jvm.workbookapp.di.AppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AudioModule
import javax.inject.Singleton

@Component(
    modules = [
        AudioModule::class,
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        TestDirectoryProviderModule::class
    ]
)
@Singleton
interface TestPersistenceComponent : AppDependencyGraph {
    fun inject(env: DatabaseEnvironment)
    fun inject(env: PersistenceEnvironment)
    fun inject(test: TestInitializeUlb)
    fun inject(test: TestInitializeProjects)
    fun inject(test: TestProjectCreate)
}
