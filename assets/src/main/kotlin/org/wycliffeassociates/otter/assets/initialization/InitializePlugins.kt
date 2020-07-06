package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import java.io.File
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository

class InitializePlugins(
    val directoryProvider: IDirectoryProvider,
    val audioPluginRegistrar: IAudioPluginRegistrar,
    val pluginRepository: IAudioPluginRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializePlugins::class.java)

    override fun exec(): Completable {
        copyOcenaudioPlugin()

        // Always import new plugins
        return ImportAudioPlugins(audioPluginRegistrar, directoryProvider)
            .importAll()
            .andThen(pluginRepository.initSelected())
            .doOnError { e ->
                log.error("Error initializing plugins", e)
            }
            .doOnComplete {
                log.info("Plugins imported!")
            }
    }

    private fun copyOcenaudioPlugin() {
        if (!File(directoryProvider.audioPluginDirectory, "ocenaudio.yaml").exists()) {
            log.info("Copying ocenaudio plugin")
            ClassLoader.getSystemResourceAsStream("plugins/ocenaudio.yaml")
                .transferTo(
                    File(
                        directoryProvider.audioPluginDirectory.absolutePath,
                        "ocenaudio.yaml"
                    ).outputStream()
                )
        } else {
            log.info("Ocenaudio plugin not initialized but ocenaudio.yaml exists in plugins directory")
        }
    }
}
