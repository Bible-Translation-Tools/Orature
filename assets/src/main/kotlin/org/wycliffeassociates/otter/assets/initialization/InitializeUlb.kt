package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository

class InitializeUlb(
    val config: Initialization?,
    val initializationRepo: IInitializationRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val directoryProvider: IDirectoryProvider,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    val rcImporter: ImportResourceContainer = ImportResourceContainer(
        resourceContainerRepo,
        directoryProvider,
        zipEntryTreeBuilder
    )
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(): Completable {
        return if (config == null || !config.initialized) {
            log.info("Initializing en_ulb...")
            rcImporter.import(ClassLoader.getSystemResourceAsStream("content/en_ulb.zip"))
                .doAfterSuccess {
                    if (config != null) {
                        config.initialized = true
                        initializationRepo.update(config).blockingAwait()
                    } else {
                        initializationRepo.insert(
                            Initialization("en_ulb", "0.0.1", true)
                        ).ignoreElement().blockingAwait()
                    }
                }
                .ignoreElement()
                .doOnComplete {
                    log.info("en_ulb imported!")
                }
                .doOnError { e ->
                    log.error("Error importing ulb.", e)
                }
                .onErrorComplete()
        } else {
            log.info("en_ulb up to date with version: ${config.version}")
            Completable.complete()
        }
    }
}