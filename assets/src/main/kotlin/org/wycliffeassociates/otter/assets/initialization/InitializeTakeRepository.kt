package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.config.Initializable
import org.wycliffeassociates.otter.common.persistence.repositories.ITakeRepository
import javax.inject.Inject

class InitializeTakeRepository @Inject constructor(
    private val takeRepository: ITakeRepository
) : Initializable {

    private val log = LoggerFactory.getLogger(InitializeTakeRepository::class.java)

    override fun exec(): Completable {
        log.info("Initializing take repository...")
        return takeRepository
            .removeNonExistentTakes()
            .andThen(takeRepository.deleteExpiredTakes())
            .doOnError { e ->
                log.error("Error initializing take repository", e)
            }
            .doOnComplete {
                log.info("Take repository initialized!")
            }
            .subscribeOn(Schedulers.io())
    }
}
