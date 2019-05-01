package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Take

interface ITakeRepository : IRepository<Take> {
    fun insertForContent(take: Take, content: Content): Single<Int>
    fun getByContent(content: Content): Single<List<Take>>
    fun removeNonExistentTakes(): Completable
    fun markDeleted(take: Take): Completable
}