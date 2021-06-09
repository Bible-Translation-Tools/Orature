package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.LANGUAGE_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.LanguageEntity

class LanguageDao(
    private val instanceDsl: DSLContext
) {

    fun fetchGateway(dsl: DSLContext = instanceDsl): List<LanguageEntity> {
        return dsl
            .select()
            .from(LANGUAGE_ENTITY)
            .where(LANGUAGE_ENTITY.GATEWAY.eq(1))
            .fetch {
                RecordMappers.mapToLanguageEntity(it)
            }
    }

    fun fetchTargets(dsl: DSLContext = instanceDsl): List<LanguageEntity> {
        return dsl
            .select()
            .from(LANGUAGE_ENTITY)
            .where(LANGUAGE_ENTITY.GATEWAY.eq(0))
            .fetch {
                RecordMappers.mapToLanguageEntity(it)
            }
    }

    fun fetchBySlug(slug: String, dsl: DSLContext = instanceDsl): LanguageEntity {
        return dsl
            .select()
            .from(LANGUAGE_ENTITY)
            .where(LANGUAGE_ENTITY.SLUG.eq(slug))
            .fetchOne {
                RecordMappers.mapToLanguageEntity(it)
            }
    }

    @Synchronized
    fun insert(entity: LanguageEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the language entity
        dsl
            .insertInto(
                LANGUAGE_ENTITY,
                LANGUAGE_ENTITY.SLUG,
                LANGUAGE_ENTITY.NAME,
                LANGUAGE_ENTITY.ANGLICIZED,
                LANGUAGE_ENTITY.DIRECTION,
                LANGUAGE_ENTITY.GATEWAY,
                LANGUAGE_ENTITY.REGION
            )
            .values(
                entity.slug,
                entity.name,
                entity.anglicizedName,
                entity.direction,
                entity.gateway,
                entity.region
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(max(LANGUAGE_ENTITY.ID))
            .from(LANGUAGE_ENTITY)
            .fetchOne {
                it.getValue(max(LANGUAGE_ENTITY.ID))
            }
    }

    @Synchronized
    fun insertAll(entities: List<LanguageEntity>, dsl: DSLContext = instanceDsl): List<Int> {
        val initialLargest = dsl
            .select(max(LANGUAGE_ENTITY.ID))
            .from(LANGUAGE_ENTITY)
            .fetchOne {
                it.getValue(max(LANGUAGE_ENTITY.ID))
            } ?: 0
        dsl.transaction { config ->
            val transactionDsl = DSL.using(config)
            entities.forEach { entity ->
                // Insert the language entity
                transactionDsl
                    .insertInto(
                        LANGUAGE_ENTITY,
                        LANGUAGE_ENTITY.SLUG,
                        LANGUAGE_ENTITY.NAME,
                        LANGUAGE_ENTITY.ANGLICIZED,
                        LANGUAGE_ENTITY.DIRECTION,
                        LANGUAGE_ENTITY.GATEWAY,
                        LANGUAGE_ENTITY.REGION
                    )
                    .values(
                        entity.slug,
                        entity.name,
                        entity.anglicizedName,
                        entity.direction,
                        entity.gateway,
                        entity.region
                    )
                    .execute()
            }
            // Implicit commit
        }
        val finalLargest = dsl
            .select(max(LANGUAGE_ENTITY.ID))
            .from(LANGUAGE_ENTITY)
            .fetchOne {
                it.getValue(max(LANGUAGE_ENTITY.ID))
            }

        // Return the ids
        return ((initialLargest + 1)..finalLargest).toList()
    }

    @Synchronized
    fun updateRegions(entities: List<LanguageEntity>, dsl: DSLContext = instanceDsl) {
        dsl.transaction { config ->
            val transactionDsl = DSL.using(config)
            entities.forEach { entity ->
                // Update region of the language entity
                transactionDsl.
                    update(LANGUAGE_ENTITY)
                    .set(LANGUAGE_ENTITY.REGION, entity.region)
                    .where(LANGUAGE_ENTITY.SLUG.eq(entity.slug))
                    .execute()
            }
        }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): LanguageEntity {
        return dsl
            .select()
            .from(LANGUAGE_ENTITY)
            .where(LANGUAGE_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToLanguageEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<LanguageEntity> {
        return dsl
            .select()
            .from(LANGUAGE_ENTITY)
            .fetch {
                RecordMappers.mapToLanguageEntity(it)
            }
    }

    fun update(entity: LanguageEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(LANGUAGE_ENTITY)
            .set(LANGUAGE_ENTITY.SLUG, entity.slug)
            .set(LANGUAGE_ENTITY.NAME, entity.name)
            .set(LANGUAGE_ENTITY.ANGLICIZED, entity.anglicizedName)
            .set(LANGUAGE_ENTITY.DIRECTION, entity.direction)
            .set(LANGUAGE_ENTITY.GATEWAY, entity.gateway)
            .set(LANGUAGE_ENTITY.REGION, entity.region)
            .where(LANGUAGE_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: LanguageEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(LANGUAGE_ENTITY)
            .where(LANGUAGE_ENTITY.ID.eq(entity.id))
            .execute()
    }
}
