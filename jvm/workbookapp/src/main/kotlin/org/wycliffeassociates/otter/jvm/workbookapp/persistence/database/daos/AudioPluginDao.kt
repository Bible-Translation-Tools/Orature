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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos

import jooq.Tables.AUDIO_PLUGIN_ENTITY
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.InsertionException
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.entities.AudioPluginEntity

class AudioPluginDao(
    private val instanceDsl: DSLContext
) {
    @Synchronized
    fun insert(entity: AudioPluginEntity, dsl: DSLContext = instanceDsl): Int {
        if (entity.id != 0) throw InsertionException("Entity ID is not 0")

        // find an existing plugin matching the name, in which we'll update
        val existing = dsl.select(AUDIO_PLUGIN_ENTITY.ID)
            .from(AUDIO_PLUGIN_ENTITY)
            .where(AUDIO_PLUGIN_ENTITY.NAME.eq(entity.name))
            .fetchOne()

        if (existing != null) {
            val id = existing.getValue(AUDIO_PLUGIN_ENTITY.ID)
            dsl
                .update(AUDIO_PLUGIN_ENTITY)
                .set(AUDIO_PLUGIN_ENTITY.VERSION, entity.version)
                .set(AUDIO_PLUGIN_ENTITY.BIN, entity.bin)
                .set(AUDIO_PLUGIN_ENTITY.ARGS, entity.args)
                .set(AUDIO_PLUGIN_ENTITY.EDIT, entity.edit)
                .set(AUDIO_PLUGIN_ENTITY.RECORD, entity.record)
                .set(AUDIO_PLUGIN_ENTITY.MARK, entity.mark)
                .set(AUDIO_PLUGIN_ENTITY.PATH, entity.path)
                .where(AUDIO_PLUGIN_ENTITY.ID.eq(id))
                .execute()
            return id
        } else {
            // Insert the plugin entity
            dsl
                .insertInto(
                    AUDIO_PLUGIN_ENTITY,
                    AUDIO_PLUGIN_ENTITY.NAME,
                    AUDIO_PLUGIN_ENTITY.VERSION,
                    AUDIO_PLUGIN_ENTITY.BIN,
                    AUDIO_PLUGIN_ENTITY.ARGS,
                    AUDIO_PLUGIN_ENTITY.EDIT,
                    AUDIO_PLUGIN_ENTITY.RECORD,
                    AUDIO_PLUGIN_ENTITY.MARK,
                    AUDIO_PLUGIN_ENTITY.PATH
                )
                .values(
                    entity.name,
                    entity.version,
                    entity.bin,
                    entity.args,
                    entity.edit,
                    entity.record,
                    entity.mark,
                    entity.path
                )
                .execute()
        }

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
            .set(AUDIO_PLUGIN_ENTITY.PATH, entity.path)
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
