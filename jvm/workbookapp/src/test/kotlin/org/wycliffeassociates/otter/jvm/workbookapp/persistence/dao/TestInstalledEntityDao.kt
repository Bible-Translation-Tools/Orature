/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.dao

import io.reactivex.Completable
import jooq.Tables.INSTALLED_ENTITY
import org.junit.Assert
import org.junit.Test
import org.wycliffeassociates.otter.common.persistence.config.Installable
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import java.io.File

class TestInstalledEntityDao {

    private val testDatabaseFile = File.createTempFile("test-db", ".sqlite").also(File::deleteOnExit)
    private val database = AppDatabase(testDatabaseFile)
    private val dao = database.installedEntityDao

    private val installable = SimpleInstallable("test", 1)
    private val installableV2 = SimpleInstallable("test", 2)
    private val notInserted = SimpleInstallable("not_here", 1)

    @Test
    fun testUpsert() {
        dao.upsert(installable)
        Assert.assertEquals(1, dao.fetchVersion(installable))
        dao.upsert(installableV2)
        Assert.assertEquals(2, dao.fetchVersion(installable))
        val rowCount = database.dsl.fetchCount(INSTALLED_ENTITY)
        Assert.assertEquals(2, rowCount)
    }

    @Test
    fun testFetchVersionNullOnNotFound() {
        Assert.assertEquals(null, dao.fetchVersion(notInserted))
    }
}

private class SimpleInstallable(override val name: String, override val version: Int) : Installable {
    override fun exec(): Completable {
        return Completable.complete()
    }
}
