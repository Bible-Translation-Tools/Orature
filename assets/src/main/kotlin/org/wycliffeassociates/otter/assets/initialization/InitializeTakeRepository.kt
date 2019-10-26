package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository

class InitializeTakeRepository(
    private val takeRepository: ITakeRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializeTakeRepository::class.java)

    override fun exec(): Completable {
        log.info("Initializing take repository...")
        return takeRepository
            .removeNonExistentTakes()
            .doOnError { e ->
                log.error("Error initializing take repository", e)
            }
            .doOnComplete {
                log.info("Take repository initialized!")
            }
    }
}