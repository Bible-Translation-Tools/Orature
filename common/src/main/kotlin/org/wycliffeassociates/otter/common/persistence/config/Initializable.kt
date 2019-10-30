package org.wycliffeassociates.otter.common.persistence.config

import io.reactivex.Completable

/**
 * Base interface for an initialization use case. Initialization use cases are tasks that are needed to configure the
 * application for either first install or updates.
 *
 *
 * @property version the version of the Initializable. As the implementing use case is updated this value should be
 * increased, and the exec method's implementation should check this against the value of what is currently initialized
 * to determine if a migration should be done.
 */
interface Initializable {

    /**
     * Executes the initializable task
     *
     * @param current the current initialization
     */
    fun exec(): Completable
}

interface Installable: Initializable {
    val name: String
    val version: Int
}