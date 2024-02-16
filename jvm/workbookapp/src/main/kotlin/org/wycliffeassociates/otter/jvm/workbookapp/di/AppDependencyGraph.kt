/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.di

import dagger.Component
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppRepositoriesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AudioModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.DirectoryProviderModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.ZipEntryTreeBuilderModule
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ImportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportChapterViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioDataStore
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AppPreferencesStore
import javax.inject.Singleton
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory
import org.wycliffeassociates.otter.jvm.device.audio.AudioDeviceProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.NarrationDebugApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.AudioWorkspaceViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.NarrationHeaderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.narration.NarrationViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AppInfoViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BlindDraftViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterReviewViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChunkingViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ConsumeViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportProjectViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel2
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.PeerEditViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RecorderViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RootViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.TranslationViewModel2

@Component(
    modules = [
        AudioModule::class,
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        DirectoryProviderModule::class,
        AppRepositoriesModule::class,
        ZipEntryTreeBuilderModule::class
    ]
)
@Singleton
interface AppDependencyGraph {
    fun inject(app: OtterApp)
    fun inject(app: NarrationDebugApp)

    fun inject(viewModel: RootViewModel)
    fun inject(viewModel: SplashScreenViewModel)
    fun inject(viewModel: HomePageViewModel2)
    fun inject(viewModel: AddPluginViewModel)
    fun inject(dataStore: AudioDataStore)
    fun inject(dataStore: AppPreferencesStore)
    fun inject(viewModel: AudioPluginViewModel)
    fun inject(viewModel: SettingsViewModel)
    fun inject(viewModel: ImportProjectViewModel)
    fun inject(viewModel: TranslationViewModel2)
    fun inject(viewModel: BookWizardViewModel)
    fun inject(viewModel: ProjectWizardViewModel)
    fun inject(viewModel: ChapterPageViewModel)
    fun inject(viewModel: AppInfoViewModel)
    fun inject(viewModel: ConsumeViewModel)
    fun inject(viewModel: ChunkingViewModel)
    fun inject(viewModel: BlindDraftViewModel)
    fun inject(viewModel: PeerEditViewModel)
    fun inject(viewModel: ChapterReviewViewModel)
    fun inject(viewModel: RecorderViewModel)
    fun inject(viewModel: ExportChapterViewModel)
    fun inject(viewModel: ExportProjectViewModel)
    fun inject(viewModel: AudioWorkspaceViewModel)
    fun inject(viewModel: NarrationViewModel)
    fun inject(viewModel: NarrationHeaderViewModel)
    fun injectDatabase(): AppDatabase
    fun injectDirectoryProvider(): IDirectoryProvider
    fun injectAppPreferencesRepository(): IAppPreferencesRepository

    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
    fun injectConnectionFactory(): AudioConnectionFactory
    fun injectAudioDeviceProvider(): AudioDeviceProvider
}
