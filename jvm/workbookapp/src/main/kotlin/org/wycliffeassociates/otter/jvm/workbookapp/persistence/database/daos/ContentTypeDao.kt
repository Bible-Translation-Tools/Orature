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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.CONTENT_TYPE
import org.jooq.DSLContext
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import java.lang.IllegalStateException
import java.util.*

class ContentTypeDao(
    instanceDsl: DSLContext
) {
    private val mapToId: Map<ContentType, Int> by lazy { loadDatabaseMap(instanceDsl) }
    private val mapToEnum: Map<Int, ContentType> by lazy { mapToId.entries.associate { (k, v) -> v to k } }

    /** This value's ID in database table content_type. */
    fun fetchId(contentType: ContentType) = mapToId[contentType]
        ?: throw IllegalStateException("$contentType is missing from ContentType table.")

    /** Get value by ID in database table content_type. */
    fun fetchForId(databaseId: Int) = mapToEnum[databaseId]

    private fun loadDatabaseMap(dsl: DSLContext): EnumMap<ContentType, Int> {
        val enumMap = EnumMap<ContentType, Int>(ContentType::class.java)

        // Get IDs from the DB
        getAll(dsl)
            .associateTo(enumMap) { it }

        // Add any missing values to DB
        ContentType.values()
            .filterNot { it in enumMap }
            .forEach { enumMap[it] = insert(it, dsl) }

        return enumMap
    }

    private fun insert(contentType: ContentType, dsl: DSLContext) = dsl
        .insertInto(CONTENT_TYPE, CONTENT_TYPE.NAME)
        .values(contentType.name)
        .returning(CONTENT_TYPE.ID)
        .fetchOne()
        .get(CONTENT_TYPE.ID)

    private fun getAll(dsl: DSLContext): List<Pair<ContentType, Int>> {
        val nameLookup = ContentType.values().associate { it.name.lowercase() to it }
        return dsl
            .select()
            .from(CONTENT_TYPE)
            .fetch { record ->
                val name = record.getValue(CONTENT_TYPE.NAME).lowercase()
                val id = record.getValue(CONTENT_TYPE.ID)
                nameLookup[name]?.let { Pair(it, id) }
            }
            .filterNotNull()
    }
}
