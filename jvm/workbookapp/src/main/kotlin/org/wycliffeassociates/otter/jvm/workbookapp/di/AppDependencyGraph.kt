package org.wycliffeassociates.otter.jvm.workbookapp.di

import dagger.Component
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IAppPreferences
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.*
import org.wycliffeassociates.otter.jvm.workbookapp.di.audio.AudioModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.persistence.DirectoryProviderModule
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.addplugin.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel.MainMenuViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectgrid.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.projectwizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.removeplugins.viewmodel.RemovePluginsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.splash.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.takemanagement.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.workbook.viewmodel.WorkbookViewModel
import javax.inject.Singleton

@Component(
    modules = [
        AudioModule::class,
        AppDatabaseModule::class,
        AppPreferencesModule::class,
        DirectoryProviderModule::class
    ]
)
@Singleton
interface AppDependencyGraph {
    fun inject(viewModel: SplashScreenViewModel)
    fun inject(viewModel: MainMenuViewModel)
    fun inject(viewModel: ProjectGridViewModel)
    fun inject(viewModel: AddPluginViewModel)
    fun inject(viewModel: WorkbookViewModel)
    fun inject(viewModel: AudioPluginViewModel)
    fun inject(viewModel: ProjectWizardViewModel)
    fun inject(viewModel: RemovePluginsViewModel)

    fun injectDatabase(): AppDatabase
    fun injectPreferences(): IAppPreferences
    fun injectDirectoryProvider(): IDirectoryProvider
    // Need inject for audio plugin repo so audio plugin registrar can be built
    fun injectAudioPluginRepository(): IAudioPluginRepository
    fun injectLanguageRepo(): ILanguageRepository
    fun injectCollectionRepo(): ICollectionRepository
    fun injectContentRepository(): IContentRepository
    fun injectResourceRepository(): IResourceRepository
    fun injectResourceContainerRepository(): IResourceContainerRepository
    fun injectResourceMetadataRepository(): IResourceMetadataRepository
    fun injectTakeRepository(): ITakeRepository
    fun injectPluginRepository(): IAudioPluginRepository
    fun injectWorkbookRepository(): IWorkbookRepository
    fun injectInstalledEntityRepository(): IInstalledEntityRepository
    fun injectZipEntryTreeBuilder(): IZipEntryTreeBuilder

    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
    fun injectRegistrar(): IAudioPluginRegistrar
}
