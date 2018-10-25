package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.AUDIO_PLUGIN_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.AudioPluginEntity

class AudioPluginDao(
        private val instanceDsl: DSLContext
) {
    @Synchronized
    fun insert(entity: AudioPluginEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // Insert the plugin entity
        dsl
                .insertInto(
                        AUDIO_PLUGIN_ENTITY,
                        AUDIO_PLUGIN_ENTITY.NAME,
                        AUDIO_PLUGIN_ENTITY.VERSION,
                        AUDIO_PLUGIN_ENTITY.BIN,
                        AUDIO_PLUGIN_ENTITY.ARGS,
                        AUDIO_PLUGIN_ENTITY.EDIT,
                        AUDIO_PLUGIN_ENTITY.RECORD
                )
                .values(
                        entity.name,
                        entity.version,
                        entity.bin,
                        entity.args,
                        entity.edit,
                        entity.record
                )
                .execute()

        // Fetch and return the resulting ID
        return dsl
                .select(max(AUDIO_PLUGIN_ENTITY.ID))
                .from(AUDIO_PLUGIN_ENTITY)
                .fetchOne {
                    it.getValue(max(AUDIO_PLUGIN_ENTITY.ID))
                }
    }

    fun fetchById(id: Int, dsl: DSLContext = instanceDsl): AudioPluginEntity {
        return dsl
                .select()
                .from(AUDIO_PLUGIN_ENTITY)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToAudioPluginEntity(it)
                }
    }

    fun fetchAll(dsl: DSLContext = instanceDsl): List<AudioPluginEntity> {
        return dsl
                .select()
                .from(AUDIO_PLUGIN_ENTITY)
                .fetch {
                    RecordMappers.mapToAudioPluginEntity(it)
                }
    }

    fun update(entity: AudioPluginEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .update(AUDIO_PLUGIN_ENTITY)
                .set(AUDIO_PLUGIN_ENTITY.NAME, entity.name)
                .set(AUDIO_PLUGIN_ENTITY.VERSION, entity.version)
                .set(AUDIO_PLUGIN_ENTITY.BIN, entity.bin)
                .set(AUDIO_PLUGIN_ENTITY.ARGS, entity.args)
                .set(AUDIO_PLUGIN_ENTITY.EDIT, entity.edit)
                .set(AUDIO_PLUGIN_ENTITY.RECORD, entity.record)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(entity.id))
                .execute()
    }

    fun delete(entity: AudioPluginEntity, dsl: DSLContext = instanceDsl) {
        dsl
                .deleteFrom(AUDIO_PLUGIN_ENTITY)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(entity.id))
                .execute()
    }
}