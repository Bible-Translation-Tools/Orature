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
package integrationtest.initialization

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import jooq.tables.InstalledEntity
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.JooqTestConfiguration
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.AppDatabase
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DATABASE_INSTALLABLE_NAME
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.DatabaseInitializer
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.SCHEMA_VERSION
import java.io.File
import kotlin.io.path.createTempDirectory

class TestInitializeDatabase {
    private val CURRENT_DB_VERSION = SCHEMA_VERSION

    private val databaseDir = createTempDirectory("otter-database").toFile()
    private val databaseFile = databaseDir.resolve("app_db.sqlite")
    private val databaseArchiveDir = databaseDir.resolve("archive")

    private val oldDatabaseDir = createTempDirectory("otter-old-database").toFile()
    private val oldDbFile = oldDatabaseDir.resolve("content.sqlite")

    private val schemaFile = File(javaClass.classLoader.getResource("sql/AppDatabaseSchema_v8.sql").file)
    private val oldSchemaFile = File(javaClass.classLoader.getResource("sql/AppDatabaseSchema0.sql").file)

    private val directoryProviderMock = mock<IDirectoryProvider> {
        on { databaseDirectory } doReturn databaseDir
        on { getUserDataDirectory(any()) } doReturn oldDatabaseDir
    }

    private val dbInitializer = DatabaseInitializer(directoryProviderMock)

    @After
    fun cleanUp() {
        databaseDir.deleteRecursively()
        databaseArchiveDir.deleteRecursively()
        oldDatabaseDir.deleteRecursively()

        Assert.assertFalse(databaseDir.exists() || oldDatabaseDir.exists())
    }

    @Before
    fun setup() {
        databaseDir.mkdirs()
        databaseArchiveDir.mkdirs()
        oldDatabaseDir.mkdirs()

        JooqTestConfiguration.createDatabase(databaseFile.absolutePath, schemaFile)
        JooqTestConfiguration.createDatabase(oldDbFile.absolutePath, oldSchemaFile)
            .dsl()
            .use {
                it.fetch("""INSERT INTO "installed_entity" VALUES ('DATABASE',1);""")
            }

        Assert.assertTrue(databaseFile.exists() && databaseFile.length() > 0)
        Assert.assertEquals(
            CURRENT_DB_VERSION,
            AppDatabase.getDatabaseVersion(databaseFile)
        )

        Assert.assertTrue(oldDbFile.exists() && oldDbFile.length() > 0)
        Assert.assertEquals(1, AppDatabase.getDatabaseVersion(oldDbFile))
    }

    @Test
    fun `test handle multiple databases exist`() {
        Assert.assertTrue(oldDbFile.exists())
        Assert.assertTrue(databaseArchiveDir.list().isEmpty())

        dbInitializer.initialize()

        Assert.assertFalse(oldDbFile.exists())
        Assert.assertTrue(databaseArchiveDir.list().isNotEmpty())
    }

    @Test
    fun `test when only old database exists`() {
        databaseFile.delete()
        Assert.assertFalse(databaseFile.exists())
        Assert.assertTrue(oldDbFile.exists())

        dbInitializer.initialize()

        Assert.assertTrue(databaseFile.exists())
        Assert.assertFalse(oldDbFile.exists())

        Assert.assertEquals(1, AppDatabase.getDatabaseVersion(databaseFile))
    }

    @Test
    fun `test when existing database has higher version than installed schema version`() {
        oldDbFile.delete()
        Assert.assertFalse(oldDbFile.exists())
        Assert.assertTrue(databaseFile.exists())

        setDatabaseVersion(CURRENT_DB_VERSION + 1, databaseFile)

        Assert.assertEquals(
            CURRENT_DB_VERSION + 1,
            AppDatabase.getDatabaseVersion(databaseFile)
        )
        Assert.assertTrue(databaseArchiveDir.list().isEmpty())

        dbInitializer.initialize()

        Assert.assertFalse(databaseFile.exists())
        Assert.assertTrue(databaseArchiveDir.list().isNotEmpty())
    }

    private fun setDatabaseVersion(version: Int, dbFile: File) {
        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${dbFile.path}"
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        DSL.using(sqLiteDataSource, SQLDialect.SQLITE).use { _dsl ->
            _dsl
                .update(InstalledEntity.INSTALLED_ENTITY)
                .set(
                    InstalledEntity.INSTALLED_ENTITY.VERSION,
                    version
                )
                .where(InstalledEntity.INSTALLED_ENTITY.NAME.eq(DATABASE_INSTALLABLE_NAME))
                .execute()
        }
    }
}