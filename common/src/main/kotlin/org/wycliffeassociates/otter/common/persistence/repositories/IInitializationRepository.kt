package org.wycliffeassociates.otter.common.persistence.repositories

interface IInitializationRepository {
    fun install(installable: Installable)
    fun getInstalledVersion(installable: Installable): Int
}