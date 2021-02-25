package org.wycliffeassociates.otter.jvm.workbookapp.di

import dagger.Component
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.common.device.IAudioRecorder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppDatabaseModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppPreferencesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AppRepositoriesModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.AudioModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.DirectoryProviderModule
import org.wycliffeassociates.otter.jvm.workbookapp.di.modules.ZipEntryTreeBuilderModule
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AddPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel.MainMenuViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectGridViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.wizard.viewmodel.ProjectWizardViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.RemovePluginsViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SplashScreenViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.AudioPluginViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookViewModel
import javax.inject.Singleton

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
    fun inject(viewModel: SplashScreenViewModel)
    fun inject(viewModel: MainMenuViewModel)
    fun inject(viewModel: ProjectGridViewModel)
    fun inject(viewModel: AddPluginViewModel)
    fun inject(viewModel: WorkbookViewModel)
    fun inject(viewModel: AudioPluginViewModel)
    fun inject(viewModel: ProjectWizardViewModel)

    fun inject(viewModel: RemovePluginsViewModel)

    fun injectDatabase(): AppDatabase
    fun injectDirectoryProvider(): IDirectoryProvider

    fun injectRecorder(): IAudioRecorder
    fun injectPlayer(): IAudioPlayer
}
