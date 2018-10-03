package org.wycliffeassociates.otter.jvm.persistence.database.daos

import jooq.Tables.AUDIO_PLUGIN_ENTITY
import jooq.Tables.LANGUAGE_ENTITY
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.persistence.entities.AudioPluginEntity
import org.wycliffeassociates.otter.jvm.persistence.entities.LanguageEntity

class AudioPluginDao(
        private val dsl: DSLContext
) : IDao<AudioPluginEntity> {
    override fun insert(entity: AudioPluginEntity): Int {
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

    override fun fetchById(id: Int): AudioPluginEntity {
        return dsl
                .select()
                .from(AUDIO_PLUGIN_ENTITY)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(id))
                .fetchOne {
                    RecordMappers.mapToAudioPluginEntity(it)
                }
    }

    override fun fetchAll(): List<AudioPluginEntity> {
        return dsl
                .select()
                .from(AUDIO_PLUGIN_ENTITY)
                .fetch {
                    RecordMappers.mapToAudioPluginEntity(it)
                }
    }

    override fun update(entity: AudioPluginEntity) {
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

    override fun delete(entity: AudioPluginEntity) {
        dsl
                .deleteFrom(AUDIO_PLUGIN_ENTITY)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(entity.id))
                .execute()
    }
}