/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.di.modules

import dagger.Binds
import dagger.Module
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.persistence.ILocaleDataSource
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
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
import org.wycliffeassociates.otter.jvm.workbookapp.domain.LocaleDataSource
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.AppPreferencesRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.AudioPluginRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.CollectionRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ContentRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.InstalledEntityRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.LanguageRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceContainerRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceMetadataRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.ResourceRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.TakeRepository
import org.wycliffeassociates.otter.jvm.workbookapp.plugin.AudioPluginRegistrar
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

    @Binds
    @Singleton
    abstract fun providesAppPreferencesRepository(
        repository: AppPreferencesRepository
    ): IAppPreferencesRepository

    @Binds
    @Singleton
    abstract fun providesLocaleDataSource(
        dataSource: LocaleDataSource
    ): ILocaleDataSource
}
