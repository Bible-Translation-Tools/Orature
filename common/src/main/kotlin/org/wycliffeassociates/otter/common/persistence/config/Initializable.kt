package org.wycliffeassociates.otter.common.persistence.config

import io.reactivex.Completable

/**
 * An initialization task that can be executed.
 */
interface Initializable {
    /**
     * Executes the initializable task
     * */
    fun exec(): Completable
}
