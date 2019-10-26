package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.config.Initialization
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.persistence.repositories.IInitializationRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository

class InitializeLanguages(
    val config: Initialization?,
    val initializationRepo: IInitializationRepository,
    val languageRepo: ILanguageRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializeLanguages::class.java)

    override fun exec(): Completable {
        return if (config == null || !config.initialized) {
            log.info("Initializing languages...")
            importLanguages()
                .doOnComplete {
                    if (config != null) {
                        config.initialized = true
                        initializationRepo.update(config).blockingAwait()
                    } else {
                        initializationRepo.insert(
                            Initialization("langnames", "0.0.1", true)
                        ).ignoreElement().blockingAwait()
                    }
                }
                .doOnError { e ->
                    log.error("Error importing languages.", e)
                }
                .doOnComplete {
                    log.info("Languages imported!")
                }
        } else {
            log.info("Languages up to date with version: ${config.version}")
            Completable.complete()
        }
    }

    private fun importLanguages(): Completable {
        return ImportLanguages(
            ClassLoader.getSystemResourceAsStream("content/langnames.json"),
            languageRepo
        ).import()
    }
}