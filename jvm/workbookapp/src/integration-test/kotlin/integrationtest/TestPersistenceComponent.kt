package integrationtest

import dagger.Component
import org.wycliffeassociates.otter.jvm.workbookapp.di.audioplugin.AudioPluginModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AudioPluginRepositoryModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.PersistenceComponent
import javax.inject.Singleton

@Component(
    modules = [
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        AudioPluginModule::class,
        AudioPluginRepositoryModule::class,
        TestDirectoryProviderModule::class
    ]
)
@Singleton
interface TestPersistenceComponent : PersistenceComponent
