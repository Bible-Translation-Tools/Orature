package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportException
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.*

private const val EN_ULB_FILENAME = "en_ulb"
private const val EN_ULB_PATH = "content/$EN_ULB_FILENAME.zip"

class InitializeUlb(
    private val installedEntityRepo: IInstalledEntityRepository,
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val resourceContainerRepo: IResourceContainerRepository,
    private val collectionRepo: ICollectionRepository,
    private val contentRepo: IContentRepository,
    private val takeRepo: ITakeRepository,
    private val languageRepo: ILanguageRepository,
    private val directoryProvider: IDirectoryProvider,
    private val zipEntryTreeBuilder: IZipEntryTreeBuilder,
    private val rcImporter: ImportResourceContainer = ImportResourceContainer(
        resourceMetadataRepo,
        resourceContainerRepo,
        collectionRepo,
        contentRepo,
        takeRepo,
        languageRepo,
        directoryProvider,
        zipEntryTreeBuilder
    )
) : Installable {

    override val name = "EN_ULB"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeUlb::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    rcImporter.import(
                        EN_ULB_FILENAME,
                        ClassLoader.getSystemResourceAsStream(EN_ULB_PATH)
                    )
                        .toObservable()
                        .blockingSubscribe(
                            { result ->
                                if (result == ImportResult.SUCCESS) {
                                    installedEntityRepo.install(this)
                                    log.info("$name version: $version installed!")
                                } else {
                                    throw ImportException(result)
                                }
                            },
                            { e ->
                                log.error("Error importing $EN_ULB_FILENAME.", e)
                            }
                        )
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
            .doOnError { e ->
                log.error("Error in initializeUlb", e)
            }
    }
}
