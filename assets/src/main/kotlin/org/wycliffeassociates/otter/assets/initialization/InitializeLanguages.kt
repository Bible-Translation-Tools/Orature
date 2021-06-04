package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.languages.ImportLanguages
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import javax.inject.Inject

const val LANGNAMES_PATH = "content/langnames.json"

class InitializeLanguages @Inject constructor(
    val installedEntityRepo: IInstalledEntityRepository,
    val languageRepo: ILanguageRepository
) : Installable {

    override val name = "LANGUAGES"
    override val version = 2

    private val log = LoggerFactory.getLogger(InitializeLanguages::class.java)

    override fun exec(): Completable {
        return Completable
            .fromCallable {
                val installedVersion = installedEntityRepo.getInstalledVersion(this)
                if (installedVersion != version) {
                    log.info("Initializing $name version: $version...")

                    migrate(installedVersion)

                    installedEntityRepo.install(this)
                    log.info("Languages imported!")
                    log.info("$name version: $version installed!")
                } else {
                    log.info("$name up to date with version: $version")
                }
            }
    }

    private fun migrate(fromVersion: Int?) {
        when (fromVersion) {
            1 -> migrate1to2()
            else -> {
                migrateTo1()
                migrate1to2()
            }
        }
    }

    private fun migrateTo1() {
        importLanguages()
            .doOnError { e ->
                log.error("Error importing languages.", e)
            }
            .blockingAwait()
    }

    private fun migrate1to2() {
        updateRegions()
            .doOnError { e ->
                log.error("Error updating regions.", e)
            }
            .blockingAwait()
    }

    private fun importLanguages(): Completable {
        return ImportLanguages(
            languageRepo
        ).import(ClassLoader.getSystemResourceAsStream(LANGNAMES_PATH))
    }

    private fun updateRegions(): Completable {
        return ImportLanguages(languageRepo)
            .updateRegions(ClassLoader.getSystemResourceAsStream(LANGNAMES_PATH))
    }
}
