package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.*
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.ResourceMetadataEntity

class ResourceMetadataDao(
    private val instanceDsl: DSLContext
) {

    fun fetchLinks(entityId: Int, dsl: DSLContext = instanceDsl): List<ResourceMetadataEntity> {
        val linkIds = dsl
            .select()
            .from(RC_LINK_ENTITY)
            .where(RC_LINK_ENTITY.RC1_FK.eq(entityId).or(RC_LINK_ENTITY.RC2_FK.eq(entityId)))
            .fetch {
                val pair = Pair(it.getValue(RC_LINK_ENTITY.RC1_FK), it.getValue(RC_LINK_ENTITY.RC2_FK))
                if (pair.first == entityId) pair.second else pair.first
            }
        return dsl
            .select()
            .from(DUBLIN_CORE_ENTITY)
            .where(DUBLIN_CORE_ENTITY.ID.`in`(linkIds))
            .fetch {
                RecordMappers.mapToResourceMetadataEntity(it)
            }
    }

    @Synchronized
    fun addLink(entity1Id: Int, entity2Id: Int, dsl: DSLContext = instanceDsl) {
        try {
            dsl
                .insertInto(RC_LINK_ENTITY, RC_LINK_ENTITY.RC1_FK, RC_LINK_ENTITY.RC2_FK)
                .values(kotlin.math.min(entity1Id, entity2Id), kotlin.math.max(entity1Id, entity2Id))
                .execute()
        } catch (e: DataAccessException) {
            // Row already exists
        }
    }

    fun removeLink(entity1Id: Int, entity2Id: Int, dsl: DSLContext = instanceDsl) {
        try {
            dsl
                .deleteFrom(RC_LINK_ENTITY)
                .where(
                    RC_LINK_ENTITY.RC1_FK.eq(kotlin.math.min(entity1Id, entity2Id))
                        .and(RC_LINK_ENTITY.RC2_FK.eq(kotlin.math.max(entity1Id, entity2Id)))
                )
                .execute()
        } catch (e: DataAccessException) {
            // No row to delete
        }
    }

    @Synchronized
    fun insert(entity: ResourceMetadataEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the resource metadata entity
        dsl
            .insertInto(
                DUBLIN_CORE_ENTITY,
                DUBLIN_CORE_ENTITY.CONFORMSTO,
                DUBLIN_CORE_ENTITY.CREATOR,
                DUBLIN_CORE_ENTITY.DESCRIPTION,
                DUBLIN_CORE_ENTITY.FORMAT,
                DUBLIN_CORE_ENTITY.IDENTIFIER,
                DUBLIN_CORE_ENTITY.ISSUED,
                DUBLIN_CORE_ENTITY.LANGUAGE_FK,
                DUBLIN_CORE_ENTITY.MODIFIED,
                DUBLIN_CORE_ENTITY.PUBLISHER,
                DUBLIN_CORE_ENTITY.SUBJECT,
                DUBLIN_CORE_ENTITY.TYPE,
                DUBLIN_CORE_ENTITY.TITLE,
                DUBLIN_CORE_ENTITY.VERSION,
                DUBLIN_CORE_ENTITY.PATH,
                DUBLIN_CORE_ENTITY.DERIVEDFROM_FK
            )
            .values(
                entity.conformsTo,
                entity.creator,
                entity.description,
                entity.format,
                entity.identifier,
                entity.issued,
                entity.languageFk,
                entity.modified,
                entity.publisher,
                entity.subject,
                entity.type,
                entity.title,
                entity.version,
                entity.path,
                entity.derivedFromFk
            )
            .execute()

        // Fetch and return the resulting ID
        return dsl
            .select(max(DUBLIN_CORE_ENTITY.ID))
            .from(DUBLIN_CORE_ENTITY)
            .fetchOne {
                it.getValue(max(DUBLIN_CORE_ENTITY.ID))
            }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): ResourceMetadataEntity {
        return dsl
            .select()
            .from(DUBLIN_CORE_ENTITY)
            .where(DUBLIN_CORE_ENTITY.ID.eq(id))
            .fetchOne {
                RecordMappers.mapToResourceMetadataEntity(it)
            }
    }

    fun fetchLatestVersion(
        languageSlug: String,
        identifier: String,
        creator: String,
        derivedFromFk: Int?,
        relaxCreatorIfNoMatch: Boolean = true,
        dsl: DSLContext = instanceDsl
    ): ResourceMetadataEntity? {
        fun flv(_creator: String?) = fetchLatestVersion(
            creator = _creator,
            languageSlug = languageSlug,
            identifier = identifier,
            derivedFromFk = derivedFromFk,
            dsl = dsl
        )

        return flv(creator)
            ?: if (relaxCreatorIfNoMatch) {
                flv(null)
            } else {
                null
            }
    }

    private fun fetchLatestVersion(
        languageSlug: String,
        identifier: String,
        creator: String?,
        derivedFromFk: Int?,
        dsl: DSLContext = instanceDsl
    ): ResourceMetadataEntity? {
        return dsl
            .select()
            .from(
                DUBLIN_CORE_ENTITY.join(LANGUAGE_ENTITY)
                    .on(DUBLIN_CORE_ENTITY.LANGUAGE_FK.eq(LANGUAGE_ENTITY.ID))
            )
            .where(LANGUAGE_ENTITY.SLUG.eq(languageSlug))
            .and(DUBLIN_CORE_ENTITY.IDENTIFIER.eq(identifier))
            .run { creator?.let { and(DUBLIN_CORE_ENTITY.CREATOR.eq(creator)) } ?: this }
            .and(
                derivedFromFk?.let(DUBLIN_CORE_ENTITY.DERIVEDFROM_FK::eq)
                    ?: DUBLIN_CORE_ENTITY.DERIVEDFROM_FK.isNull
            )
            .orderBy(DUBLIN_CORE_ENTITY.VERSION.desc())
            .limit(1)
            .fetchOne {
                RecordMappers.mapToResourceMetadataEntity(it)
            }
    }

    fun fetchLatestVersion(
        languageSlug: String,
        identifier: String,
        dsl: DSLContext = instanceDsl
    ): ResourceMetadataEntity? {
        return dsl
            .select()
            .from(
                DUBLIN_CORE_ENTITY.join(LANGUAGE_ENTITY)
                    .on(DUBLIN_CORE_ENTITY.LANGUAGE_FK.eq(LANGUAGE_ENTITY.ID))
            )
            .where(LANGUAGE_ENTITY.SLUG.eq(languageSlug))
            .and(DUBLIN_CORE_ENTITY.IDENTIFIER.eq(identifier))
            .orderBy(DUBLIN_CORE_ENTITY.VERSION.desc())
            .limit(1)
            .fetchOne {
                RecordMappers.mapToResourceMetadataEntity(it)
            }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<ResourceMetadataEntity> {
        return dsl
            .select()
            .from(DUBLIN_CORE_ENTITY)
            .fetch {
                RecordMappers.mapToResourceMetadataEntity(it)
            }
    }

    fun update(entity: ResourceMetadataEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .update(DUBLIN_CORE_ENTITY)
            .set(DUBLIN_CORE_ENTITY.CONFORMSTO, entity.conformsTo)
            .set(DUBLIN_CORE_ENTITY.CREATOR, entity.creator)
            .set(DUBLIN_CORE_ENTITY.DESCRIPTION, entity.description)
            .set(DUBLIN_CORE_ENTITY.FORMAT, entity.format)
            .set(DUBLIN_CORE_ENTITY.IDENTIFIER, entity.identifier)
            .set(DUBLIN_CORE_ENTITY.ISSUED, entity.issued)
            .set(DUBLIN_CORE_ENTITY.LANGUAGE_FK, entity.languageFk)
            .set(DUBLIN_CORE_ENTITY.MODIFIED, entity.modified)
            .set(DUBLIN_CORE_ENTITY.PUBLISHER, entity.publisher)
            .set(DUBLIN_CORE_ENTITY.SUBJECT, entity.subject)
            .set(DUBLIN_CORE_ENTITY.TYPE, entity.type)
            .set(DUBLIN_CORE_ENTITY.TITLE, entity.title)
            .set(DUBLIN_CORE_ENTITY.VERSION, entity.version)
            .set(DUBLIN_CORE_ENTITY.PATH, entity.path)
            .set(DUBLIN_CORE_ENTITY.DERIVEDFROM_FK, entity.derivedFromFk)
            .where(DUBLIN_CORE_ENTITY.ID.eq(entity.id))
            .execute()
    }

    fun delete(entity: ResourceMetadataEntity, dsl: DSLContext = instanceDsl) {
        dsl
            .deleteFrom(DUBLIN_CORE_ENTITY)
            .where(DUBLIN_CORE_ENTITY.ID.eq(entity.id))
            .execute()
    }
}