package org.wycliffeassociates.otter.common.persistence.mapping

interface Mapper<E, D> {
    fun mapFromEntity(type: E): D
    fun mapToEntity(type: D): E
}