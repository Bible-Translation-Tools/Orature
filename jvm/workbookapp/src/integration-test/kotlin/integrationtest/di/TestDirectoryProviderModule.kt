package integrationtest.di

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import java.io.File

@Module
class TestDirectoryProviderModule {
    private val root = createTempDir("orature-test").also(File::deleteOnExit)

    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider(
        appName = "Orature",
        userHome = root.resolve("user").apply { mkdirs() }.path,
        windowsAppData = root.resolve("workbookapp").apply { mkdirs() }.path
    )
}
