package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.common.persistence.repositories.WorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.AudioPluginRegistrar
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.AudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.CollectionRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ContentRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.InstalledEntityRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.LanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceContainerRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.TakeRepository
import javax.inject.Singleton

@Module
abstract class AppRepositoriesModule {
    @Binds
    @Singleton
    abstract fun providesLanguageRepo(
        repository: LanguageRepository
    ): ILanguageRepository

    @Binds
    @Singleton
    abstract fun providesCollectionRepo(
        repository: CollectionRepository
    ): ICollectionRepository

    @Binds
    @Singleton
    abstract fun providesContentRepository(
        repository: ContentRepository
    ): IContentRepository

    @Binds
    @Singleton
    abstract fun providesResourceRepository(
        repository: ResourceRepository
    ): IResourceRepository

    @Binds
    @Singleton
    abstract fun providesResourceContainerRepository(
        repository: ResourceContainerRepository
    ): IResourceContainerRepository

    @Binds
    @Singleton
    abstract fun providesResourceMetadataRepository(
        repository: ResourceMetadataRepository
    ): IResourceMetadataRepository

    @Binds
    @Singleton
    abstract fun providesTakeRepository(
        repository: TakeRepository
    ): ITakeRepository

    @Binds
    @Singleton
    abstract fun providesPluginRepository(
        repository: AudioPluginRepository
    ): IAudioPluginRepository

    @Binds
    @Singleton
    abstract fun providesWorkbookRepository(
        repository: WorkbookRepository
    ): IWorkbookRepository

    @Binds
    @Singleton
    abstract fun providesInstalledEntityRepository(
        repository: InstalledEntityRepository
    ): IInstalledEntityRepository

    @Binds
    @Singleton
    abstract fun providesRegistrar(
        registrar: AudioPluginRegistrar
    ): IAudioPluginRegistrar
}
