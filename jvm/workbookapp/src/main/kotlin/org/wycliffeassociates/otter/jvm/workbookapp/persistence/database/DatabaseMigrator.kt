package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import jooq.tables.AudioPluginEntity
import jooq.tables.InstalledEntity
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory

const val SCHEMA_VERSION = 2
const val DATABASE_INSTALLABLE_NAME = "database"

class DatabaseMigrator {
    val logger = LoggerFactory.getLogger(DatabaseMigrator::class.java)

    fun migrate(dsl: DSLContext) {
        var currentVersion = getDatabaseVersion(dsl)
        if (currentVersion != SCHEMA_VERSION) {
            currentVersion = migrate0to1(dsl, currentVersion)
            currentVersion = migrate1to2(dsl, currentVersion)
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
                .fetch { record -> record.getValue(InstalledEntity.INSTALLED_ENTITY.VERSION) }
                .first()
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
                ).values(DATABASE_INSTALLABLE_NAME, 1)
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
            try {
                dsl
                    .alterTable(AudioPluginEntity.AUDIO_PLUGIN_ENTITY)
                    .addColumn(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
                    .execute()
                logger.info("Updated database from version 1 to 2")
            } catch (e: DataAccessException) {
                // Exception is thrown because the column might already exist but an existence check cannot
                // be performed in sqlite.
            }
            return 2
        } else {
            current
        }
    }
}
