@file:Suppress("FunctionNaming")
package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Translation
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.common.persistence.repositories.ILanguageRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import javax.inject.Inject

class InitializeTranslations @Inject constructor(
    private val installedEntityRepo: IInstalledEntityRepository,
    private val workbookRepository: IWorkbookRepository,
    private val languageRepository: ILanguageRepository
) : Installable {
    override val name = "TRANSLATIONS"
    override val version = 1

    private val log = LoggerFactory.getLogger(InitializeTranslations::class.java)

    override fun exec(): Completable {
        return Completable.fromCallable {
            var installedVersion = installedEntityRepo.getInstalledVersion(this)
            if (installedVersion != version) {
                log.info("Initializing $name version: $version...")

                migrate()

                installedEntityRepo.install(this)
                log.info("$name version: $version installed!")
            } else {
                log.info("$name up to date with version: $version")
            }
        }
    }

    private fun migrate() {
        `migrate to version 1`()
    }

    private fun `migrate to version 1`() {
        val projects = fetchProjects()
        projects.map(::insertTranslation)
    }

    private fun fetchProjects(): List<Workbook> {
        return workbookRepository.getProjects()
            .doOnError { e ->
                log.error("Error in loading projects", e)
            }
            .blockingGet()
    }

    private fun insertTranslation(workBook: Workbook) {
        val translation = Translation(workBook.source.language, workBook.target.language)
        languageRepository
            .insertTranslation(translation)
            .doOnError { e ->
                log.error("Error in inserting translation", e)
            }
            .onErrorReturnItem(0)
            .blockingGet()
    }
}
