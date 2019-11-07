package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository

private const val EN_ULB_FILENAME = "en_ulb"
private const val EN_ULB_PATH = "content/$EN_ULB_FILENAME.zip"

class InitializeUlb(
    val installedEntityRepo: IInstalledEntityRepository,
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
            val installedVersion = installedEntityRepo.getInstalledVersion(this)
            if (installedVersion != version) {
                log.info("Initializing $name version: $version...")
                rcImporter.import(
                    EN_ULB_FILENAME,
                    ClassLoader.getSystemResourceAsStream(EN_ULB_PATH)
                )
                    .map { result ->
                        if (result == ImportResult.SUCCESS) {
                            installedEntityRepo.install(this)
                            log.info("$name version: $version installed!")
                        } else {
                            throw ImportException(result)
                        }
                    }
                    .ignoreElement()
                    .doOnComplete {
                        log.info("$EN_ULB_FILENAME imported!")
                    }
                    .onErrorComplete { e ->
                        log.error("Error importing $EN_ULB_FILENAME.", e)
                        return@onErrorComplete true
                    }
                    .blockingAwait()
            } else {
                log.info("$name up to date with version: $version")
            }
        }
    }
}