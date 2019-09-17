package integrationtest.rcimport

import dagger.Module
import dagger.Provides
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.persistence.DirectoryProvider
import java.io.File

@Module
class TestDirectoryProviderModule {
    private val root = createTempDir("otter-test").also(File::deleteOnExit)

    @Provides
    fun providesDirectoryProvider(): IDirectoryProvider = DirectoryProvider(
        appName = "TranslationRecorder",
        userHome = root.resolve("user").apply { mkdirs() }.path,
        windowsAppData = root.resolve("app").apply { mkdirs() }.path
    )
}
