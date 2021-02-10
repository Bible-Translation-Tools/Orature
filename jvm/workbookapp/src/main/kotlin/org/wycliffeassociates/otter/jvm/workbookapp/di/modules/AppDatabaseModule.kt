package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.workbookapp.audioplugin.AudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.workbookapp.domain.resourcecontainer.project.ZipEntryTreeBuilder
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.*
import java.io.File
import javax.inject.Singleton

@Module
class AppDatabaseModule {
    @Provides
    @Singleton
    fun providesAppDatabase(
        directoryProvider: IDirectoryProvider
    ): AppDatabase {
        return AppDatabase(
            directoryProvider
                .getAppDataDirectory()
                .resolve(File("content.sqlite"))
        )
    }

    @Provides
    @Singleton
    fun providesLanguageRepo(
        directoryProvider: IDirectoryProvider
    ): ILanguageRepository {
        val db = providesAppDatabase(directoryProvider)
        return LanguageRepository(db)
    }

    @Provides
    @Singleton
    fun providesCollectionRepo(
        directoryProvider: IDirectoryProvider
    ): ICollectionRepository {
        val db = providesAppDatabase(directoryProvider)
        return CollectionRepository(db, directoryProvider)
    }

    @Provides
    @Singleton
    fun providesContentRepository(
        directoryProvider: IDirectoryProvider
    ): IContentRepository {
        val db = providesAppDatabase(directoryProvider)
        return ContentRepository(db)
    }

    @Provides
    @Singleton
    fun providesResourceRepository(
        directoryProvider: IDirectoryProvider
    ): IResourceRepository {
        val db = providesAppDatabase(directoryProvider)
        return ResourceRepository(db)
    }

    @Provides
    @Singleton
    fun providesResourceContainerRepository(
        directoryProvider: IDirectoryProvider
    ): IResourceContainerRepository {
        val db = providesAppDatabase(directoryProvider)
        val collectionRepository = providesCollectionRepo(directoryProvider)
        val resourceRepository = providesResourceRepository(directoryProvider)
        return ResourceContainerRepository(db, collectionRepository, resourceRepository)
    }

    @Provides
    @Singleton
    fun providesResourceMetadataRepository(
        directoryProvider: IDirectoryProvider
    ): IResourceMetadataRepository {
        val db = providesAppDatabase(directoryProvider)
        return ResourceMetadataRepository(db)
    }

    @Provides
    @Singleton
    fun providesTakeRepository(
        directoryProvider: IDirectoryProvider
    ): ITakeRepository {
        val db = providesAppDatabase(directoryProvider)
        return TakeRepository(db)
    }

    @Provides
    fun providesPluginRepository(
        directoryProvider: IDirectoryProvider,
        appPreferences: IAppPreferences
    ): IAudioPluginRepository {
        val db = providesAppDatabase(directoryProvider)
        return AudioPluginRepository(db, appPreferences)
    }

    @Provides
    @Singleton
    fun providesWorkbookRepository(
        directoryProvider: IDirectoryProvider
    ): IWorkbookRepository {
        val db = providesAppDatabase(directoryProvider)
        val collectionRepo = CollectionRepository(db, directoryProvider)
        val contentRepository = ContentRepository(db)
        val resourceRepository = ResourceRepository(db)
        val resourceMetadataRepository = ResourceMetadataRepository(db)
        val takeRepository = TakeRepository(db)
        val resourceContainerRepository = ResourceContainerRepository(
            db,
            collectionRepo,
            resourceRepository
        )
        return WorkbookRepository(
            collectionRepo,
            contentRepository,
            resourceRepository,
            resourceMetadataRepository,
            takeRepository
        )
    }

    @Provides
    @Singleton
    fun providesInstalledEntityRepository(
        directoryProvider: IDirectoryProvider
    ): IInstalledEntityRepository {
        val db = providesAppDatabase(directoryProvider)
        return InstalledEntityRepository(db)
    }

    @Provides
    fun providesZipEntryTreeBuilder(): IZipEntryTreeBuilder {
        return ZipEntryTreeBuilder
    }

    @Provides
    fun providesRegistrar(audioPluginRepository: IAudioPluginRepository): IAudioPluginRegistrar =
        AudioPluginRegistrar(audioPluginRepository)
}
