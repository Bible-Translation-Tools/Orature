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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import jooq.tables.*
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.data.primitives.CheckingStatus as CheckingStatusEnum

const val SCHEMA_VERSION = 13
const val DATABASE_INSTALLABLE_NAME = "DATABASE"

class DatabaseMigrator {
    val logger = LoggerFactory.getLogger(DatabaseMigrator::class.java)

    fun migrate(dsl: DSLContext) {
        var currentVersion = getDatabaseVersion(dsl)
        if (currentVersion != SCHEMA_VERSION) {
            if (currentVersion <= 8) { // Ot1
                extractSelectedTakeInfo(dsl)
            }
            currentVersion = migrate0to1(dsl, currentVersion)
            currentVersion = migrate1to2(dsl, currentVersion)
            currentVersion = migrate2to3(dsl, currentVersion)
            currentVersion = migrate3to4(dsl, currentVersion)
            currentVersion = migrate4to5(dsl, currentVersion)
            currentVersion = migrate5to6(dsl, currentVersion)
            currentVersion = migrate6to7(dsl, currentVersion)
            currentVersion = migrate7to8(dsl, currentVersion)
            currentVersion = migrate8to9(dsl, currentVersion)
            currentVersion = migrate9to10(dsl, currentVersion)
            currentVersion = migrate10to11(dsl, currentVersion)
            currentVersion = migrate11to12(dsl, currentVersion)
            currentVersion = migrate12to13(dsl, currentVersion)
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

    private fun extractSelectedTakeInfo(dsl: DSLContext) {
//        val pathsToSelected = dsl
//            .select()
//            .from(ContentEntity.CONTENT_ENTITY)
//            .join(TakeEntity.TAKE_ENTITY)
//            .on(ContentEntity.CONTENT_ENTITY.SELECTED_TAKE_FK.eq(TakeEntity.TAKE_ENTITY.ID))
//            .fetch(TakeEntity.TAKE_ENTITY.PATH)

        val res = dsl
            .fetch("""
                select take_entity.path 
                from content_entity 
                join take_entity 
                on content_entity.selected_take_fk = take_entity.id
                """.trimIndent()
            )
            .getValues(TakeEntity.TAKE_ENTITY.PATH.name)

        println(res)
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

    /**
     * Version 8
     * Adds a column for the source rate and target rate to the translations table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate7to8(dsl: DSLContext, current: Int): Int {
        return if (current < 8) {
            try {
                dsl
                    .alterTable(TranslationEntity.TRANSLATION_ENTITY)
                    .addColumn(TranslationEntity.TRANSLATION_ENTITY.SOURCE_RATE)
                    .execute()
                dsl
                    .alterTable(TranslationEntity.TRANSLATION_ENTITY)
                    .addColumn(TranslationEntity.TRANSLATION_ENTITY.TARGET_RATE)
                    .execute()
                logger.info("Updated database from version 7 to 8")
            } catch (e: DataAccessException) {
                // Exception is thrown because the column might already exist but an existence check cannot
                // be performed in sqlite.
                logger.error("Error in migrate7to8", e)
            }
            return 8
        } else {
            current
        }
    }

    /**
     * Version 9
     * Adds a column for the draft number to the collections table
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate8to9(dsl: DSLContext, current: Int): Int {
        return if (current < 9) {
            try {
                dsl
                    .alterTable(ContentEntity.CONTENT_ENTITY)
                    .addColumn(ContentEntity.CONTENT_ENTITY.DRAFT_NUMBER)
                    .execute()
                logger.info("Updated database from version 8 to 9")
                return 9
            } catch (e: DataAccessException) {
                // Exception is thrown because the column might already exist but an existence check cannot
                // be performed in sqlite.
                logger.error("Error in migrate8to9", e)
                return 8
            }
        } else {
            current
        }
    }
    
     /**
     * Version 10
     * Adds a table for Versification
     */
    private fun migrate9to10(dsl: DSLContext, current: Int): Int {
         return if (current < 10) {
             dsl
                 .createTableIfNotExists(
                     VersificationEntity.VERSIFICATION_ENTITY
                 )
                 .column(VersificationEntity.VERSIFICATION_ENTITY.ID)
                 .column(VersificationEntity.VERSIFICATION_ENTITY.SLUG)
                 .column(VersificationEntity.VERSIFICATION_ENTITY.PATH)
                 .constraints(
                     DSL.primaryKey(VersificationEntity.VERSIFICATION_ENTITY.ID),
                     DSL.unique(VersificationEntity.VERSIFICATION_ENTITY.SLUG)
                 )
                 .execute()
             logger.info("Updated database from version 9 to 10")
             return 10
         } else current
     }

    /**
     * Version 11
     * Adds a column for the bridged and v_end to the content table
     *
     * The tables related to projects are truncated, which effectively is deleting the database. This is because
     * verse bridges and verse end are difficult to construct and migration code is nontrivial. As projects existing
     * in the project directory but not in the database are re-imported, this serves as an alternative to database
     * migrations here.
     *
     * The DataAccessException is caught in the event that the column already exists.
     */
    private fun migrate10to11(dsl: DSLContext, current: Int): Int {
        return if (current < 11) {
            try {
                dsl
                    .alterTable(ContentEntity.CONTENT_ENTITY)
                    .addColumn(ContentEntity.CONTENT_ENTITY.BRIDGED)
                    .execute()

                dsl
                    .alterTable(ContentEntity.CONTENT_ENTITY)
                    .addColumn(ContentEntity.CONTENT_ENTITY.V_END)
                    .execute()

                clearProjectTables(dsl)

                logger.info("Updated database from version 10 to 11")
                return 11
            } catch (e: DataAccessException) {
                // Exception is thrown because the column might already exist but an existence check cannot
                // be performed in sqlite.
                logger.error("Error in migrate10to11", e)
                return 10
            }
        } else {
            current
        }
    }

    /**
     * Version 12
     * Adds WorkbookDescriptor table and WorkbookType table
     */
    private fun migrate11to12(dsl: DSLContext, current: Int): Int {
        return if (current < 12) {
            createWorkbookTypeTable(dsl)
            createWorkbookDescriptorTable(dsl)
            logger.info("Updated database from version 11 to 12")
            12
        } else {
            current
        }
    }

    /**
     * Version 13
     * Adds Checking Status table and Take Entity's FK column reference.
     */
    private fun migrate12to13(dsl: DSLContext, current: Int): Int {
        return if (current < 13) {
            dsl
                .createTableIfNotExists(CheckingStatus.CHECKING_STATUS)
                .column(CheckingStatus.CHECKING_STATUS.ID)
                .column(CheckingStatus.CHECKING_STATUS.NAME)
                .constraints(
                    DSL.primaryKey(CheckingStatus.CHECKING_STATUS.ID),
                    DSL.unique(CheckingStatus.CHECKING_STATUS.NAME)
                )
                .execute()

            seedCheckingStatus(dsl)

            /** Default value for new column in Take Entity */
            val uncheckedId = dsl.select(CheckingStatus.CHECKING_STATUS.ID)
                .from(CheckingStatus.CHECKING_STATUS)
                .where(
                    CheckingStatus.CHECKING_STATUS.NAME
                        .eq(CheckingStatusEnum.UNCHECKED.name)
                )
                .fetchOne()!!
                .get(CheckingStatus.CHECKING_STATUS.ID)

            try {
                /**
                 * Since ADD CONSTRAINT is not supported in SQLite - https://www.sqlite.org/omitted.html,
                 * we copy the data to another table (same fields), drop the original table and rename
                 * the new table back to the original one.
                 */

                dsl.createTable("take_entity_temp")
                    .column(TakeEntity.TAKE_ENTITY.ID)
                    .column(TakeEntity.TAKE_ENTITY.CONTENT_FK)
                    .column(TakeEntity.TAKE_ENTITY.FILENAME)
                    .column(TakeEntity.TAKE_ENTITY.PATH)
                    .column(TakeEntity.TAKE_ENTITY.NUMBER)
                    .column(TakeEntity.TAKE_ENTITY.CREATED_TS)
                    .column(TakeEntity.TAKE_ENTITY.DELETED_TS)
                    .column(TakeEntity.TAKE_ENTITY.PLAYED)
                    .column(TakeEntity.TAKE_ENTITY.CHECKING_FK, SQLDataType.INTEGER.defaultValue(uncheckedId))
                    .column(TakeEntity.TAKE_ENTITY.CHECKSUM, SQLDataType.VARCHAR.nullable(true))
                    .constraints(
                        DSL.primaryKey(TakeEntity.TAKE_ENTITY.ID),
                        DSL.constraint("fk_checking_status")
                            .foreignKey(TakeEntity.TAKE_ENTITY.CHECKING_FK)
                            .references(CheckingStatus.CHECKING_STATUS)
                    )
                    .execute()

                dsl.insertInto(DSL.table("take_entity_temp"))
                    .select(
                        dsl
                            .select(
                                TakeEntity.TAKE_ENTITY.ID,
                                TakeEntity.TAKE_ENTITY.CONTENT_FK,
                                TakeEntity.TAKE_ENTITY.FILENAME,
                                TakeEntity.TAKE_ENTITY.PATH,
                                TakeEntity.TAKE_ENTITY.NUMBER,
                                TakeEntity.TAKE_ENTITY.CREATED_TS,
                                TakeEntity.TAKE_ENTITY.DELETED_TS,
                                TakeEntity.TAKE_ENTITY.PLAYED,
                                DSL.inline(uncheckedId), // Default value for CHECKING_FK
                                DSL.inline(null, TakeEntity.TAKE_ENTITY.CHECKSUM) // Default value for CHECKSUM
                            )
                            .from(TakeEntity.TAKE_ENTITY)
                    )
                    .execute()

                dsl.dropTable(TakeEntity.TAKE_ENTITY)
                    .execute()

                dsl.alterTable(DSL.table("take_entity_temp"))
                    .renameTo(TakeEntity.TAKE_ENTITY.name)
                    .execute()

            } catch (e: DataAccessException) {
                // Exception is thrown because the column might already exist but an existence check cannot
                // be performed in sqlite.
                logger.error("Error in while migrating database from version 12 to 13", e)
                return 12
            }
            logger.info("Updated database from version 12 to 13")
            13
        } else {
            current
        }
    }

    private fun createWorkbookTypeTable(dsl: DSLContext) {
        dsl
            .createTableIfNotExists(
                WorkbookType.WORKBOOK_TYPE
            )
            .column(WorkbookType.WORKBOOK_TYPE.ID)
            .column(WorkbookType.WORKBOOK_TYPE.NAME)
            .constraints(
                DSL.primaryKey(WorkbookType.WORKBOOK_TYPE.ID),
                DSL.unique(WorkbookType.WORKBOOK_TYPE.NAME)
            )
            .execute()
    }

    private fun createWorkbookDescriptorTable(dsl: DSLContext) {
        dsl
            .createTableIfNotExists(
                WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY
            )
            .column(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.ID)
            .column(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK)
            .column(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK)
            .column(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK)
            .constraints(
                DSL.primaryKey(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.ID),
                DSL.unique(
                    WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK,
                    WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK,
                    WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK
                ),
                DSL.foreignKey(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.SOURCE_FK)
                    .references(CollectionEntity.COLLECTION_ENTITY).onDeleteCascade(),
                DSL.foreignKey(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TARGET_FK)
                    .references(CollectionEntity.COLLECTION_ENTITY).onDeleteCascade(),
                DSL.foreignKey(WorkbookDescriptorEntity.WORKBOOK_DESCRIPTOR_ENTITY.TYPE_FK)
                    .references(WorkbookType.WORKBOOK_TYPE)
            )
            .execute()
    }

    private fun seedCheckingStatus(dsl: DSLContext) {
        dsl
            .insertInto(
                CheckingStatus.CHECKING_STATUS,
                CheckingStatus.CHECKING_STATUS.NAME
            )
            .also { insert ->
                CheckingStatusEnum.values().forEach { status ->
                    insert.values(status.name)
                }
            }
            .execute()
    }

    private fun clearProjectTables(dsl: DSLContext) {
        dsl
            .deleteFrom(TakeEntity.TAKE_ENTITY)
            .execute()

        dsl
            .deleteFrom(ContentDerivative.CONTENT_DERIVATIVE)
            .execute()

        dsl
            .deleteFrom(ContentEntity.CONTENT_ENTITY)
            .execute()

        dsl
            .deleteFrom(CollectionEntity.COLLECTION_ENTITY)
            .execute()

        dsl
            .deleteFrom(ResourceLink.RESOURCE_LINK)
            .execute()

        dsl
            .deleteFrom(DublinCoreEntity.DUBLIN_CORE_ENTITY)
            .execute()
    }

    companion object {
        var selectedTakeFiles = listOf<String>()
            private set
    }
}
