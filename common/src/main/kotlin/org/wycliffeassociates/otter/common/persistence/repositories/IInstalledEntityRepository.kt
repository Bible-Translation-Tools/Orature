package org.wycliffeassociates.otter.common.persistence.repositories

import org.wycliffeassociates.otter.common.persistence.config.Installable

interface IInstalledEntityRepository {
    fun install(entity: Installable)
    fun getInstalledVersion(entity: Installable): Int?
}