/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.tables.CheckingStatus as checkingStatusTable
import org.jooq.DSLContext
import java.lang.IllegalStateException
import java.util.*
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus

class CheckingStatusDao(instanceDsl: DSLContext) {
    private val mapToId: Map<CheckingStatus, Int> by lazy { loadToDatabase(instanceDsl) }
    private val mapToEnum: Map<Int, CheckingStatus> by lazy { mapToId.entries.associate { (k, v) -> v to k } }

    fun fetchId(mode: CheckingStatus): Int {
        return mapToId[mode]
            ?: throw IllegalStateException("Mode: $mode does not exist in database table.")
    }

    fun fetchById(databaseId: Int) = mapToEnum[databaseId]

    private fun loadToDatabase(dsl: DSLContext): EnumMap<CheckingStatus, Int> {
        val enumMap = EnumMap<CheckingStatus, Int>(CheckingStatus::class.java)
        val existingEntities = getAll(dsl)
        enumMap.putAll(existingEntities)

        CheckingStatus.values()
            .filterNot { it in enumMap } // exclude existing items
            .forEach { enumMap[it] = insert(it, dsl) } // insert new items to db

        return enumMap
    }

    private fun insert(checkingStatus: CheckingStatus, dsl: DSLContext): Int {
        return dsl
            .insertInto(
                checkingStatusTable.CHECKING_STATUS,
                checkingStatusTable.CHECKING_STATUS.NAME
            )
            .values(checkingStatus.name)
            .returning(checkingStatusTable.CHECKING_STATUS.ID)
            .fetchOne()!!
            .get(checkingStatusTable.CHECKING_STATUS.ID)
    }

    private fun getAll(dsl: DSLContext): Map<CheckingStatus, Int> {
        return dsl
            .select()
            .from(checkingStatusTable.CHECKING_STATUS)
            .fetch()
            .mapNotNull { record ->
                val name = record.getValue(checkingStatusTable.CHECKING_STATUS.NAME)
                val id = record.getValue(checkingStatusTable.CHECKING_STATUS.ID)
                CheckingStatus.get(name)?.let {
                    Pair(it, id)
                }
            }
            .associate { it }
    }
}