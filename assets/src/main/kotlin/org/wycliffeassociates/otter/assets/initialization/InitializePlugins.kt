package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.domain.plugins.IAudioPluginRegistrar
import org.wycliffeassociates.otter.common.domain.plugins.ImportAudioPlugins
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import java.io.File

class InitializePlugins(
    val config: Initialization?,
    val directoryProvider: IDirectoryProvider,
    val audioPluginRegistrar: IAudioPluginRegistrar,
    val pluginRepository: IAudioPluginRepository,
    val initializationRepository: IInitializationRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializePlugins::class.java)

    override fun exec(): Completable {
        if (config == null || !config.initialized) {
            if (!File(directoryProvider.audioPluginDirectory, "ocenaudio.yaml").exists()) {
                log.info("Initializing ocenaudio plugin")
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
            initializationRepository.insert(
                Initialization("ocenaudio", "0.0.1", true)
            ).blockingGet()
        }

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
}