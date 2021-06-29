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
package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import jooq.Tables
import jooq.tables.AudioPluginEntity
import jooq.tables.InstalledEntity
import jooq.tables.LanguageEntity
import org.jooq.DSLContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DATABASE_INSTALLABLE_NAME
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DatabaseMigrator
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.SCHEMA_VERSION
import java.io.File
import java.lang.Exception

class TestDatabaseMigrator {

    var dsl: DSLContext? = null
    val dbFile = File.createTempFile("database", ".sqlite").apply { deleteOnExit() }
    val schemaFile = File.createTempFile("schema0", ".sql").apply { deleteOnExit() }
    val latestVersion = SCHEMA_VERSION

    @Before
    fun setup() {
        ClassLoader
            .getSystemResourceAsStream("sql/AppDatabaseSchema0.sql")
            .copyTo(schemaFile.outputStream())
        dsl = JooqTestConfiguration.createDatabase(dbFile.absolutePath, schemaFile).dsl()
        // Make sure the database file has the tables we need
        val sqlStatements = schemaFile
            .bufferedReader()
            .readText()
            .split(";")
            .filter { it.isNotBlank() }
            .map { "$it;" }

        // Execute each SQL statement
        sqlStatements.forEach {
            dsl!!.fetch(it)
        }
    }

    @After
    fun teardown() {
        schemaFile.delete()
        dbFile.delete()
    }

    @Test
    fun `test migrate from version 0 to 2`() {
        dsl?.let { _dsl ->
            _dsl
                .insertInto(
                    Tables.AUDIO_PLUGIN_ENTITY,
                    Tables.AUDIO_PLUGIN_ENTITY.NAME,
                    Tables.AUDIO_PLUGIN_ENTITY.VERSION,
                    Tables.AUDIO_PLUGIN_ENTITY.BIN,
                    Tables.AUDIO_PLUGIN_ENTITY.ARGS,
                    Tables.AUDIO_PLUGIN_ENTITY.EDIT,
                    Tables.AUDIO_PLUGIN_ENTITY.RECORD,
                    Tables.AUDIO_PLUGIN_ENTITY.PATH
                )
                .values(
                    "test plugin",
                    "1.0.0",
                    "bin",
                    "",
                    1,
                    1,
                    "path"
                )
                .execute()

            // Test that marker column does not exist before migration
            var exceptionThrown = false
            try {
                var pluginRecord = _dsl.select().from(AudioPluginEntity.AUDIO_PLUGIN_ENTITY).fetchOne()
                pluginRecord.fields().contains(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
            } catch (e: Exception) {
                Assert.assertTrue(
                    "Exception thrown that mark column does not exist",
                    e.message!!.contains("no such column: audio_plugin_entity.mark")
                )
                exceptionThrown = true
            }
            Assert.assertTrue(
                "Mark field exception thrown to verify marker column does not exist",
                exceptionThrown
            )

            try {
                DatabaseMigrator().migrate(_dsl)
            } catch (e: Exception) {
            }

            // Test that marker exists and the default value is provided after migration
            val pluginRecord = _dsl.select().from(AudioPluginEntity.AUDIO_PLUGIN_ENTITY).fetchOne()
            Assert.assertEquals(
                true,
                pluginRecord.fields().contains(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
            )
            val canMark = pluginRecord.getValue(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
            Assert.assertEquals(canMark, 0)
        } ?: Assert.fail()
    }

    @Test
    fun `test migrate from version 2 to 3`() {
        dsl?.let { _dsl ->
            _dsl
                .insertInto(
                    Tables.LANGUAGE_ENTITY,
                    Tables.LANGUAGE_ENTITY.SLUG,
                    Tables.LANGUAGE_ENTITY.NAME,
                    Tables.LANGUAGE_ENTITY.GATEWAY,
                    Tables.LANGUAGE_ENTITY.ANGLICIZED,
                    Tables.LANGUAGE_ENTITY.DIRECTION
                )
                .values(
                    "en",
                    "english",
                    1,
                    "english",
                    "ltr"
                )
                .execute()

            // Test that region column does not exist before migration
            var exceptionThrown = false
            try {
                var languageRecord = _dsl.select().from(LanguageEntity.LANGUAGE_ENTITY).fetchOne()
                languageRecord.fields().contains(LanguageEntity.LANGUAGE_ENTITY.REGION)
            } catch (e: Exception) {
                Assert.assertTrue(
                    "Exception thrown that region column does not exist",
                    e.message!!.contains("no such column: language_entity.region")
                )
                exceptionThrown = true
            }
            Assert.assertTrue(
                "Mark field exception thrown to verify region column does not exist",
                exceptionThrown
            )

            try {
                DatabaseMigrator().migrate(_dsl)
            } catch (e: Exception) {
            }

            // Test that region exists and the default value is null
            val languageRecord = _dsl.select().from(LanguageEntity.LANGUAGE_ENTITY).fetchOne()
            Assert.assertEquals(
                true,
                languageRecord.fields().contains(LanguageEntity.LANGUAGE_ENTITY.REGION)
            )
            val region = languageRecord.getValue(LanguageEntity.LANGUAGE_ENTITY.REGION)
            Assert.assertEquals(region, null)
        } ?: Assert.fail()
    }

    @Test
    fun `migrated to the latest version`() {
        dsl?.let { _dsl ->
            try {
                DatabaseMigrator().migrate(_dsl)
            } catch (e: Exception) {
            }

            // Test that database version is latest version
            val databaseVersionRecord = _dsl
                .select()
                .from(InstalledEntity.INSTALLED_ENTITY)
                .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                .fetchSingle()

            Assert.assertNotNull(databaseVersionRecord)
            val version = databaseVersionRecord.getValue(InstalledEntity.INSTALLED_ENTITY.VERSION)
            Assert.assertEquals("Assert version is migrated from 0 to $latestVersion", latestVersion, version)
        }
    }
}
