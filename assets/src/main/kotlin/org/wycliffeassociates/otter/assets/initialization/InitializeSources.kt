package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import java.io.File
import javax.inject.Inject

class InitializeSources @Inject constructor(
    private val directoryProvider: IDirectoryProvider,
    private val resourceMetadataRepo: IResourceMetadataRepository,
    private val installedEntityRepo: IInstalledEntityRepository,
    private val rcImporter: ImportResourceContainer
): Installable {

    override val name = "SOURCES"
    override val version = 1

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun exec(): Completable {
        return Completable
            .fromAction {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    logger.info("Initializing sources...")

                    importSources(directoryProvider.internalSourceRCDirectory)

                    installedEntityRepo.install(this)
                    logger.info("$name version: $version installed!")
                } else {
                    logger.info("$name up to date with version: $version")
                }
            }
    }

    private fun importSources(dir: File) {
        if (dir.isFile || !dir.exists()) return
        val existingPaths = fetchSourcePaths()

        dir.walk().filter {
            it.isFile && it !in existingPaths
        }.forEach {
            // Find resource containers to import
            if (it.extension in OratureFileFormat.extensionList) {
                importFile(it)
            }
        }
    }

    private fun fetchSourcePaths(): List<File> {
        return resourceMetadataRepo
            .getAllSources()
            .blockingGet()
            .map {
                it.path
            }
    }

    private fun importFile(file: File) {
        rcImporter.import(file).toObservable()
            .doOnError { e ->
                logger.error("Error importing $file.", e)
            }
            .blockingSubscribe {
                logger.info("${file.name} imported!")
            }
    }
}