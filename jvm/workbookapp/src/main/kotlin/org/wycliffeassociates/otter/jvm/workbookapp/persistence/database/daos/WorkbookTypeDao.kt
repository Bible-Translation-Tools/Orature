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

import jooq.tables.WorkbookType.WORKBOOK_TYPE
import org.jooq.DSLContext
import org.wycliffeassociates.otter.common.data.primitives.ProjectMode
import java.lang.IllegalStateException
import java.util.*

class WorkbookTypeDao(instanceDsl: DSLContext) {
    private val mapToId: Map<ProjectMode, Int> by lazy { loadToDatabase(instanceDsl) }
    private val mapToEnum: Map<Int, ProjectMode> by lazy { mapToId.entries.associate { (k, v) -> v to k } }

    fun fetchId(mode: ProjectMode): Int {
        return mapToId[mode]
            ?: throw IllegalStateException("Mode: $mode does not exist in database table.")
    }

    fun fetchById(databaseId: Int) = mapToEnum[databaseId]

    private fun loadToDatabase(dsl: DSLContext): EnumMap<ProjectMode, Int> {
        val enumMap = EnumMap<ProjectMode, Int>(ProjectMode::class.java)
        val existingEntities = getAll(dsl)
        enumMap.putAll(existingEntities)

        ProjectMode.values()
            .filterNot { it in enumMap } // exclude existing items
            .forEach { enumMap[it] = insert(it, dsl) } // insert new items to db

        return enumMap
    }

    private fun insert(mode: ProjectMode, dsl: DSLContext): Int {
        return dsl
            .insertInto(WORKBOOK_TYPE, WORKBOOK_TYPE.NAME)
            .values(mode.name)
            .returning(WORKBOOK_TYPE.ID)
            .fetchOne()!!
            .get(WORKBOOK_TYPE.ID)
    }

    private fun getAll(dsl: DSLContext): Map<ProjectMode, Int> {
        return dsl
            .select()
            .from(WORKBOOK_TYPE)
            .fetch()
            .mapNotNull { record ->
                val name = record.getValue(WORKBOOK_TYPE.NAME)
                val id = record.getValue(WORKBOOK_TYPE.ID)
                ProjectMode.get(name)?.let {
                    Pair(it, id)
                }
            }
            .associate { it }
    }
}
