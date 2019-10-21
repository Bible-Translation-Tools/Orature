package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.config.Initialization

interface IInitializationRepository : IRepository<Initialization> {
    fun insert(initialization: Initialization): Single<Int>
}