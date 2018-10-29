package org.wycliffeassociates.otter.common.persistence.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take

interface ITakeRepository : IRepository<Take> {
    fun insertForChunk(take: Take, chunk: Chunk): Single<Int>
    fun getByChunk(chunk: Chunk): Single<List<Take>>
    fun removeNonExistentTakes(): Completable
}