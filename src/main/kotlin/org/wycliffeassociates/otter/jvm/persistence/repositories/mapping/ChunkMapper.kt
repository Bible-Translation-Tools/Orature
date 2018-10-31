package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Chunk
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.persistence.entities.ChunkEntity

class ChunkMapper {
    fun mapFromEntity(entity: ChunkEntity, selectedTake: Take?, end: Int): Chunk {
        return Chunk(
                entity.sort,
                entity.labelKey,
                entity.start,
                end,
                selectedTake,
                entity.id
        )
    }

    fun mapToEntity(obj: Chunk, collectionFk: Int = 0): ChunkEntity {
        return ChunkEntity(
                obj.id,
                obj.sort,
                obj.labelKey,
                obj.start,
                collectionFk,
                obj.selectedTake?.id
        )
    }

}