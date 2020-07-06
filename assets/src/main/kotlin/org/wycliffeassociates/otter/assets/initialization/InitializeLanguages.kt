package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository

const val LANGNAMES_PATH = "content/langnames.json"

class InitializeLanguages(
    val installedEntityRepo: IInstalledEntityRepository,
    val languageRepo: ILanguageRepository
) : Installable {

    override val name = "LANGUAGES"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeLanguages::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")
                    importLanguages()
                        .doOnComplete {
                            installedEntityRepo.install(this)
                            log.info("Languages imported!")
                            log.info("$name version: $version installed!")
                        }
                        .doOnError { e ->
                            log.error("Error importing languages.", e)
                        }
                        .blockingAwait()
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
    }

    private fun importLanguages(): Completable {
        return ImportLanguages(
            ClassLoader.getSystemResourceAsStream(LANGNAMES_PATH),
            languageRepo
        ).import()
    }
}
