package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Marker
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.persistence.entities.TakeEntity
import java.io.File
import java.time.LocalDate

class TakeMapper {
    fun mapFromEntity(entity: TakeEntity, markers: List<Marker>): Take {
        return Take(
            filename = entity.filename,
            path = File(entity.filepath),
            number = entity.number,
            created = entity.createdTs.let(LocalDate::parse),
            deleted = entity.deletedTs?.let(LocalDate::parse),
            played = entity.played == 1,
            markers = markers,
            id = entity.id
        )
    }

    fun mapToEntity(obj: Take, contentFk: Int = -1): TakeEntity {
        return TakeEntity(
            id = obj.id,
            contentFk = contentFk,
            filename = obj.filename,
            filepath = obj.path.toURI().path,
            number = obj.number,
            createdTs = obj.created.toString(),
            deletedTs = obj.deleted?.toString(),
            played = if (obj.played) 1 else 0
        )
    }
}