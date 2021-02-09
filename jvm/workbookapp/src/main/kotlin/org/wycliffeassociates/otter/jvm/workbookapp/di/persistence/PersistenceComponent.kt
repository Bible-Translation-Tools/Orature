package org.wycliffeassociates.otter.jvm.workbookapp.di.persistence

import dagger.Component
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import javax.inject.Singleton

//@Component(
//    modules = [
//        AppDatabaseModule::class,
//        AppPreferencesModule::class,
//        AudioPluginRepositoryModule::class,
//        DirectoryProviderModule::class
//    ]
//)
//interface PersistenceComponent {
//    fun injectDatabase(): AppDatabase
//    fun injectPreferences(): IAppPreferences
//    fun injectDirectoryProvider(): IDirectoryProvider
//    // Need inject for audio plugin repo so audio plugin registrar can be built
//    fun injectAudioPluginRepository(): IAudioPluginRepository
//    fun injectLanguageRepo(): ILanguageRepository
//    fun injectCollectionRepo(): ICollectionRepository
//    fun injectContentRepository(): IContentRepository
//    fun injectResourceRepository(): IResourceRepository
//    fun injectResourceContainerRepository(): IResourceContainerRepository
//    fun injectResourceMetadataRepository(): IResourceMetadataRepository
//    fun injectTakeRepository(): ITakeRepository
//    fun injectPluginRepository(): IAudioPluginRepository
//    fun injectWorkbookRepository(): IWorkbookRepository
//    fun injectInstalledEntityRepository(): IInstalledEntityRepository
//
//    fun injectZipEntryTreeBuilder(): IZipEntryTreeBuilder
//
//}
