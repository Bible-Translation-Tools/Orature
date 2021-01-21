package org.wycliffeassociates.otter.jvm.workbookapp.persistence

import jooq.Tables
import jooq.tables.AudioPluginEntity
import jooq.tables.InstalledEntity
import org.jooq.DSLContext
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DatabaseMigrator
import java.io.File
import java.lang.Exception

class TestDatabaseMigrator {

    var dsl: DSLContext? = null
    val dbFile = File.createTempFile("database", ".sqlite").apply { deleteOnExit() }
    val schemaFile = File.createTempFile("schema0", ".sql").apply { deleteOnExit() }

    @Before
    fun setup() {
        ClassLoader.getSystemResourceAsStream("sql/AppDatabaseSchema0.sql").copyTo(schemaFile.outputStream())
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


            DatabaseMigrator().migrate(_dsl)

            // Test that database version is version 2
            val databaseVersionRecord = _dsl.select().from(InstalledEntity.INSTALLED_ENTITY).where(InstalledEntity.INSTALLED_ENTITY.NAME.eq("database")).fetchOne()
            Assert.assertNotNull(databaseVersionRecord)
            val version = databaseVersionRecord.getValue(InstalledEntity.INSTALLED_ENTITY.VERSION)
            Assert.assertEquals("Assert version is migrated from 0 to 2", 2, version)

            // Test that marker exists and the default value is provided after migration
            val pluginRecord = _dsl.select().from(AudioPluginEntity.AUDIO_PLUGIN_ENTITY).fetchOne()
            Assert.assertEquals(true, pluginRecord.fields().contains(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK))
            val canMark = pluginRecord.getValue(AudioPluginEntity.AUDIO_PLUGIN_ENTITY.MARK)
            Assert.assertEquals(canMark, 0)
        } ?: Assert.fail()
    }
}
