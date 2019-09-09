package integrationtest.rcimport

import dagger.Component
import org.wycliffeassociates.otter.jvm.persistence.injection.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.persistence.injection.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.persistence.injection.AudioPluginRepositoryModule
import org.wycliffeassociates.otter.jvm.persistence.injection.PersistenceComponent
import javax.inject.Singleton

@Component(
    modules = [
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        AudioPluginRepositoryModule::class,
        TestDirectoryProviderModule::class
    ]
)
@Singleton
interface TestPersistenceComponent : PersistenceComponent
