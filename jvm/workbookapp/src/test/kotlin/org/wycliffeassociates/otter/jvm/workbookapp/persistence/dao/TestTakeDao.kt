/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.TakeDao
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.CollectionEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.TakeEntity
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.TakeRepository
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.CollectionMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.MarkerMapper
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.repositories.mapping.TakeMapper
import java.io.File
import java.time.LocalDate

class TestTakeDao {
    private val testDatabaseFile = File.createTempFile("test-db", ".sqlite").also(File::deleteOnExit)
    private val database = AppDatabase(testDatabaseFile)
    private val dao = database.takeDao

    private val files = listOf(
        File("take1.wav"),
        File("take2.wav"),
        File("take3.wav"),
        File("take4.wav")
    )

    @Before
    fun setup() {
        for (f in files) {
            f.createNewFile()
            f.outputStream().write(1)
            f.deleteOnExit()
        }
        database.dsl.execute("PRAGMA foreign_keys = OFF;")
        for ((idx, f) in files.withIndex()) {
            dao.insert(TakeEntity(0, idx % 2, f.name, f.absolutePath, idx, LocalDate.now().toString(), null, 0))
        }
    }

    @After
    fun teardown() {
        files.forEach { it.delete() }
        dao.fetchAll().forEach { dao.delete(it) }
    }

    @Test
    fun `test setting deletedTs qualifies as soft delete`() {
        val softDeletes = arrayListOf<TakeEntity>()
        var deletedCount = 0
        for (i in files.indices.plus(1)) {
            if (i % 2 == 0) {
                val idx = i + 1
                dao.softDeleteTake(dao.fetchById(idx))
                val softdelete = dao.fetchById(idx)
                softDeletes.add(softdelete)
                deletedCount += 1
                assertTrue("Take $idx is deleted set to true", softdelete.deletedTs.isNullOrEmpty().not())
                assertTrue("Take $idx is a soft deleted take", dao.fetchSoftDeletedTakes().contains(dao.fetchById(idx)))
            }
        }
        assertTrue("fetch soft deletes fetches all with deletedTs", dao.fetchSoftDeletedTakes().containsAll(softDeletes))
        assertEquals("fetch soft deletes count matches deleted takes", dao.fetchSoftDeletedTakes().size, softDeletes.size)
        val remaining = dao.fetchAll().minus(dao.fetchSoftDeletedTakes())
        assertEquals("remaining takes exist", remaining.size, files.size - deletedCount)
    }

    @Test
    fun `test fetch soft deleted takes by content`() {
        val contentFkToDelete = 0
        val takes = dao.fetchAll()
        takes.forEach { if (it.contentFk % 2 == 0) dao.softDeleteTake(it) }
        val deletedTakes = dao.fetchSoftDeletedTakes(CollectionEntity(contentFkToDelete, null, null, "", "", "", 0, null))
        deletedTakes.forEach {
            assertTrue("Soft deleted take matches content fk", it.contentFk == contentFkToDelete)
        }
        val remaining = dao.fetchAll().minus(deletedTakes).size
        assertTrue("Takes without content fk remain", remaining > 0)
    }

    @Test
    fun `test delete expired takes`() {
        for (i in files.indices.plus(1)) {
            if (i % 2 == 0) {
                val idx = i + 1
                dao.softDeleteTake(dao.fetchById(idx))
                assertTrue("Take $idx is deleted set to true", dao.fetchById(idx).deletedTs.isNullOrEmpty().not())
                assertTrue("Take $idx is a soft deleted take", dao.fetchSoftDeletedTakes().contains(dao.fetchById(idx)))
            }
        }
        TakeRepository(database, TakeMapper(), MarkerMapper(), CollectionMapper()).deleteExpiredTakes(0).blockingGet()
        assertTrue("Soft Deleted Takes are gone", dao.fetchSoftDeletedTakes().isEmpty())
        for ((idx, f) in files.withIndex()) {
            if (idx % 2 == 0) {
                assertTrue("Deleted File deleted", !f.exists())
            } else {
                assertTrue("Remaining file exists", f.exists())
            }
        }
    }

    private fun TakeDao.softDeleteTake(take: TakeEntity, expiry: LocalDate = LocalDate.now()) {
        update(take.apply { deletedTs = expiry.toString() })
    }
}
