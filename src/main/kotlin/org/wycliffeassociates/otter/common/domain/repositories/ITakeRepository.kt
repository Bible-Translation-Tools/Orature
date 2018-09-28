package org.wycliffeassociates.otter.common.domain.repositories

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take

interface ITakeRepository : IRepository<Take> {
    fun getByChunk(chunk: Chunk): Single<List<Take>>
}