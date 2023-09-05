package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.tables.CheckingLevel as CheckingLevelTable
import org.jooq.DSLContext
import java.lang.IllegalStateException
import java.util.*
import org.wycliffeassociates.otter.common.data.primitives.CheckingLevel

class CheckingLevelDao(instanceDsl: DSLContext) {
    private val mapToId: Map<CheckingLevel, Int> by lazy { loadToDatabase(instanceDsl) }
    private val mapToEnum: Map<Int, CheckingLevel> by lazy { mapToId.entries.associate { (k, v) -> v to k } }

    fun fetchId(mode: CheckingLevel) = mapToId[mode]
        ?: throw IllegalStateException("Mode: $mode does not exist in database table.")

    fun fetchById(databaseId: Int) = mapToEnum[databaseId]

    private fun loadToDatabase(dsl: DSLContext): EnumMap<CheckingLevel, Int> {
        val enumMap = EnumMap<CheckingLevel, Int>(CheckingLevel::class.java)
        val existingEntities = getAll(dsl)
        enumMap.putAll(existingEntities)

        CheckingLevel.values()
            .filterNot { it in enumMap } // exclude existing items
            .forEach { enumMap[it] = insert(it, dsl) } // insert new items to db

        return enumMap
    }

    private fun insert(checkingLevel: CheckingLevel, dsl: DSLContext): Int {
        return dsl
            .insertInto(CheckingLevelTable.CHECKING_LEVEL, CheckingLevelTable.CHECKING_LEVEL.NAME)
            .values(checkingLevel.name)
            .returning(CheckingLevelTable.CHECKING_LEVEL.ID)
            .fetchOne()!!
            .get(CheckingLevelTable.CHECKING_LEVEL.ID)
    }

    private fun getAll(dsl: DSLContext): Map<CheckingLevel, Int> {
        return dsl
            .select()
            .from(CheckingLevelTable.CHECKING_LEVEL)
            .fetch()
            .mapNotNull { record ->
                val name = record.getValue(CheckingLevelTable.CHECKING_LEVEL.NAME)
                val id = record.getValue(CheckingLevelTable.CHECKING_LEVEL.ID)
                CheckingLevel.get(name)?.let {
                    Pair(it, id)
                }
            }
            .associate { it }
    }
}