package org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories

import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.common.persistence.repositories.IInstalledEntityRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import javax.inject.Inject

class InstalledEntityRepository @Inject constructor(
    private val database: AppDatabase
) : IInstalledEntityRepository {

    private val installedEntityDao = database.installedEntityDao

    override fun install(entity: Installable) {
        installedEntityDao.upsert(entity)
    }

    override fun getInstalledVersion(entity: Installable): Int? {
        return installedEntityDao.fetchVersion(entity)
    }
}
