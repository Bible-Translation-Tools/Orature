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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import jooq.tables.AudioPluginEntity
import jooq.tables.CollectionEntity
import jooq.tables.DublinCoreEntity
import jooq.tables.InstalledEntity
import jooq.tables.LanguageEntity
import jooq.tables.TranslationEntity
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory

const val SCHEMA_VERSION = 7
const val DATABASE_INSTALLABLE_NAME = "DATABASE"

class DatabaseMigrator {
    val logger = LoggerFactory.getLogger(DatabaseMigrator::class.java)

    fun migrate(dsl: DSLContext) {
        var currentVersion = getDatabaseVersion(dsl)
        if (currentVersion != SCHEMA_VERSION) {
            currentVersion = migrate0to1(dsl, currentVersion)
            currentVersion = migrate1to2(dsl, currentVersion)
            currentVersion = migrate2to3(dsl, currentVersion)
            currentVersion = migrate3to4(dsl, currentVersion)
            currentVersion = migrate4to5(dsl, currentVersion)
            currentVersion = migrate5to6(dsl, currentVersion)
            currentVersion = migrate6to7(dsl, currentVersion)

            updateDatabaseVersion(dsl, currentVersion)
        }
    }

    private fun getDatabaseVersion(dsl: DSLContext): Int {
        val databaseVersionExists =
            dsl
                .fetchExists(
                    dsl
                        .select()
                        .from(InstalledEntity.INSTALLED_ENTITY)
                        .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                )
        return if (databaseVersionExists) {
            dsl
                .select()
                .from(InstalledEntity.INSTALLED_ENTITY)
                .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                .fetchSingle { record -> record.getValue(InstalledEntity.INSTALLED_ENTITY.VERSION) }
        } else 0
    }

    private fun updateDatabaseVersion(dsl: DSLContext, version: Int) {
        dsl
            .update(InstalledEntity.INSTALLED_ENTITY)
            .set(InstalledEntity.INSTALLED_ENTITY.VERSION, version)
            .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
            .execute()
    }

    /**
     * Version 1
     * introduces the database itself as an "installed entity" to store the version number
     * to facilitate future database migrations
     */
    private fun migrate0to1(dsl: DSLContext, current: Int): Int {
        return if (current < 1) {
            dsl
                .insertInto(
                    InstalledEntity.INSTALLED_ENTITY,
                    InstalledEntity.INSTALLED_ENTITY.NAME,
                    InstalledEntity.INSTALLED_ENTITY.VERSION
                )
                .values(DATABASE_INSTALLABLE_NAME, 1)
                .execute()
            logger.info("Updated database from version 0 to 1")
            return 1
        } else current
    }

    /**
     * Version 2
     * Adds a column for the marker plugin to the audio plugin table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate1to2(dsl: DSLContext, current: Int): Int {
        return if (current < 2) {
            dsl
                .alterTable(AudioPluginEntity.AUDIO_PLUGIN_ENTITY)
                .addColumn(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
                .execute()
            logger.info("Updated database from version 1 to 2")
            return 2
        } else {
            current
        }
    }

    /**
     * Version 3
     * Adds a column for the region to the languages table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate2to3(dsl: DSLContext, current: Int): Int {
        return if (current < 3) {
            dsl
                .alterTable(LanguageEntity.LANGUAGE_ENTITY)
                .addColumn(LanguageEntity.LANGUAGE_ENTITY.REGION)
                .execute()
            logger.info("Updated database from version 2 to 3")
            return 3
        } else {
            current
        }
    }

    /**
     * Version 4
     * Create translation table
     */
    private fun migrate3to4(dsl: DSLContext, current: Int): Int {
        return if (current < 4) {
            dsl
                .createTableIfNotExists(
                    TranslationEntity.TRANSLATION_ENTITY
                )
                .column(TranslationEntity.TRANSLATION_ENTITY.ID)
                .column(TranslationEntity.TRANSLATION_ENTITY.SOURCE_FK)
                .column(TranslationEntity.TRANSLATION_ENTITY.TARGET_FK)
                .constraints(
                    DSL.primaryKey(TranslationEntity.TRANSLATION_ENTITY.ID),
                    DSL.unique(
                        TranslationEntity.TRANSLATION_ENTITY.SOURCE_FK,
                        TranslationEntity.TRANSLATION_ENTITY.TARGET_FK
                    ),
                    DSL.foreignKey(TranslationEntity.TRANSLATION_ENTITY.SOURCE_FK)
                        .references(LanguageEntity.LANGUAGE_ENTITY),
                    DSL.foreignKey(TranslationEntity.TRANSLATION_ENTITY.TARGET_FK)
                        .references(LanguageEntity.LANGUAGE_ENTITY)
                )
                .execute()
            logger.info("Updated database from version 3 to 4")
            return 4
        } else current
    }

    /**
     * Version 5
     * Adds a column for the rights to the dublin core table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate4to5(dsl: DSLContext, current: Int): Int {
        return if (current < 5) {
            dsl
                .alterTable(DublinCoreEntity.DUBLIN_CORE_ENTITY)
                .addColumn(DublinCoreEntity.DUBLIN_CORE_ENTITY.LICENSE)
                .execute()
            logger.info("Updated database from version 4 to 5")
            return 5
        } else {
            current
        }
    }

    /**
     * Version 6
     * Adds a column for the modified timestamp to the translations table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate5to6(dsl: DSLContext, current: Int): Int {
        return if (current < 6) {
            dsl
                .alterTable(TranslationEntity.TRANSLATION_ENTITY)
                .addColumn(TranslationEntity.TRANSLATION_ENTITY.MODIFIED_TS)
                .execute()
            logger.info("Updated database from version 5 to 6")
            return 6
        } else {
            current
        }
    }

    /**
     * Version 7
     * Adds a column for the modified timestamp to the collection table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate6to7(dsl: DSLContext, current: Int): Int {
        return if (current < 7) {
            dsl
                .alterTable(CollectionEntity.COLLECTION_ENTITY)
                .addColumn(CollectionEntity.COLLECTION_ENTITY.MODIFIED_TS)
                .execute()
            logger.info("Updated database from version 6 to 7")
            return 7
        } else {
            current
        }
    }
}
