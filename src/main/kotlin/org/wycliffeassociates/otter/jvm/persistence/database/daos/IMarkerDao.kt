package org.wycliffeassociates.otter.jvm.persistence.database.daos

import org.wycliffeassociates.otter.jvm.persistence.entities.MarkerEntity

interface IMarkerDao : IDao<MarkerEntity> {
    fun fetchByTakeId(id: Int): List<MarkerEntity>
}