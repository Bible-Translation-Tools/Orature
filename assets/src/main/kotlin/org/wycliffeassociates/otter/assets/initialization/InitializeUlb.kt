package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository

const val EN_ULB = "en_ulb"

class InitializeUlb(
    val initializationRepo: IInitializationRepository,
    val resourceContainerRepo: IResourceContainerRepository,
    val directoryProvider: IDirectoryProvider,
    val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    val rcImporter: ImportResourceContainer = ImportResourceContainer(
        resourceContainerRepo,
        directoryProvider,
        zipEntryTreeBuilder
    )
) : Installable {

    override val name = "EN_ULB"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(): Completable {
        return Completable.fromCallable {
            val installedVersion = initializationRepo.getInstalledVersion(this)
            if (installedVersion != version) {
                log.info("Initializing $EN_ULB...")
                rcImporter.import(EN_ULB, ClassLoader.getSystemResourceAsStream("content/$EN_ULB.zip"))
                    .doAfterSuccess {
                        initializationRepo.install(this)
                    }
                    .ignoreElement()
                    .doOnComplete {
                        log.info("$EN_ULB imported!")
                    }
                    .doOnError { e ->
                        log.error("Error importing $EN_ULB.", e)
                    }
            } else {
                log.info("en_ulb up to date with version: $version")
                Completable.complete()
            }
        }
    }
}