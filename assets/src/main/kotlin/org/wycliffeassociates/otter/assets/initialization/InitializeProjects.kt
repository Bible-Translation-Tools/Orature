package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.IZipEntryTreeBuilder
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import org.wycliffeassociates.otter.common.persistence.repositories.ICollectionRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IContentRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceContainerRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IResourceMetadataRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import java.io.File
import java.nio.file.Path

class InitializeProjects(
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
): Initializable {
    private val log = LoggerFactory.getLogger(InitializeProjects::class.java)

    override fun exec(): Completable {
        return Completable.fromCallable {
            val projects = collectionRepo.getDerivedProjects().blockingGet()
            if (projects.isEmpty()) {
                log.info("Initializing projects...")

                val dir = directoryProvider.getUserDataDirectory("/")
                importProjects(dir)
            }
        }
    }

    private fun importProjects(dir: File) {
        if (dir.isFile) return

        dir.listFiles()?.forEach {
            // Find resource containers to import
            val manifest = Path.of("manifest.yaml")
            if (it.isFile && it.toPath().contains(manifest)) {
                importProject(it.parentFile)
            }
            importProjects(it)
        }
    }

    private fun importProject(project: File) {
        rcImporter.import(project).toObservable()
            .doOnError { e ->
                log.error("Error importing ${project.name}.", e)
            }
            .blockingSubscribe {
                log.info("${project.name} imported!")
            }
    }
}
