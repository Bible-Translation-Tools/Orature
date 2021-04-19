package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping

import org.wycliffeassociates.otter.common.data.primitives.Marker
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.MarkerEntity
import javax.inject.Inject

class MarkerMapper @Inject constructor() {
    fun mapFromEntity(type: MarkerEntity): Marker {
        return Marker(
            type.number,
            type.position,
            type.label,
            type.id
        )
    }

    fun mapToEntity(type: Marker, takeFk: Int? = null): MarkerEntity {
        return MarkerEntity(
            type.id,
            takeFk,
            type.number,
            type.position,
            type.label
        )
    }
}
