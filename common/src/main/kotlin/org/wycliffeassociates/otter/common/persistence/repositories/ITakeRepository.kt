package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Take
import java.sql.Timestamp

interface ITakeRepository : IRepository<Take> {
    fun insertForContent(take: Take, content: Content): Single<Int>
    fun getByContent(content: Content, includeDeleted: Boolean = false): Single<List<Take>>
    fun removeNonExistentTakes(): Completable
    fun markDeleted(take: Take): Completable
    fun getSoftDeletedTakes(project: Collection): Single<List<Take>>
    fun deleteExpiredTakes(expiry: Int = 0): Completable
}
