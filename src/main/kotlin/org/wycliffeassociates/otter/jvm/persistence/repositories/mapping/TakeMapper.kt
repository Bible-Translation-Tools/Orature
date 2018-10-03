package org.wycliffeassociates.otter.jvm.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.model.Marker
import org.wycliffeassociates.otter.common.data.model.Take
import org.wycliffeassociates.otter.jvm.persistence.entities.TakeEntity
import java.io.File
import java.time.LocalDate

class TakeMapper {
    fun mapFromEntity(entity: TakeEntity, markers: List<Marker>): Take {
        return Take(
                entity.filename,
                File(entity.filepath),
                entity.number,
                LocalDate.parse(entity.timestamp),
                entity.played == 1,
                markers,
                entity.id
        )
    }

    fun mapToEntity(obj: Take): TakeEntity {
        return TakeEntity(
                obj.id,
                null,
                obj.filename,
                obj.path.toURI().path,
                obj.number,
                obj.timestamp.toString(),
                if (obj.played) 1 else 0
        )
    }
}