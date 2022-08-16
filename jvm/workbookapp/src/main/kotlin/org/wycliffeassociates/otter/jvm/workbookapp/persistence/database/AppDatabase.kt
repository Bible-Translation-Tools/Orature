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

import jooq.tables.InstalledEntity
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import org.sqlite.SQLiteDataSource
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.database.daos.*
import java.io.File
import java.io.IOException
import java.nio.file.Files

const val CREATION_SCRIPT = "sql/CreateAppDb.sql"

class AppDatabase(
    private val databaseFile: File,
    private val directoryProvider: IDirectoryProvider
) {
    val logger = LoggerFactory.getLogger(AppDatabase::class.java)

    lateinit var dsl: DSLContext

    init {
        System.setProperty("org.jooq.no-logo", "true")

        // Load the SQLite JDBC drivers
        Class
            .forName("org.sqlite.JDBC")
            .getDeclaredConstructor()
            .newInstance()

        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${databaseFile.toURI().path}"
        // Enable foreign key constraints (disabled by default for backwards compatibility)
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")

        handleDatabaseMigration(sqLiteDataSource)
    }

    private fun handleDatabaseMigration(
        sqLiteDataSource: SQLiteDataSource
    ) {
        val dbMigrator = DatabaseMigrator()
        var dslContext: DSLContext? = null

        val oldDbFile = directoryProvider.getUserDataDirectory().resolve("content.sqlite")
        val oldDbExist = oldDbFile.exists() && oldDbFile.length() > 0
        val currentDbExists = databaseFile.exists() && databaseFile.length() > 0

        when {
            oldDbExist && currentDbExists -> {
                archiveDb(databaseFile)
                Files.move(oldDbFile.toPath(), databaseFile.toPath())
            }
            oldDbExist -> {
                Files.move(oldDbFile.toPath(), databaseFile.toPath())
            }
            currentDbExists -> {
                val conn = sqLiteDataSource.connection
                DSL.using(conn, SQLDialect.SQLITE).use { _dsl ->
                    val currentVersion = dbMigrator.getDatabaseVersion(_dsl)
                    if (currentVersion > SCHEMA_VERSION) {
                        _dsl.close()
                        conn.close()
                        archiveDb(databaseFile)
                    }
                }
            }
            else -> {
                dslContext = DSL.using(sqLiteDataSource.connection, SQLDialect.SQLITE)
                dsl = dslContext
                setup()
            }
        }

        dsl = dslContext ?: DSL.using(sqLiteDataSource.connection, SQLDialect.SQLITE)
        dbMigrator.migrate(dsl)
    }

    private fun archiveDb(dbFile: File) {
        val archive = directoryProvider.databaseDirectory
            .resolve("archive")
            .let {
                it.mkdirs()
                it.resolve(
                    "app_db-${System.currentTimeMillis()}.sqlite"
                )
            }

        Files.move(
            dbFile.toPath(),
            archive.toPath()
        )
    }

    private fun setup() {
        // Setup the tables
        val schemaFileStream = ClassLoader.getSystemResourceAsStream(CREATION_SCRIPT)
            ?: throw IOException("Couldn't read database creation script $CREATION_SCRIPT")

        // Make sure the database file has the tables we need
        val sqlStatements = schemaFileStream
            .bufferedReader()
            .readText()
            .split(";")
            .filter { it.isNotBlank() }
            .map { "$it;" }

        // Execute each SQL statement
        sqlStatements.forEach {
            dsl.fetch(it)
        }

        dsl.insertInto(
            InstalledEntity.INSTALLED_ENTITY,
            InstalledEntity.INSTALLED_ENTITY.NAME,
            InstalledEntity.INSTALLED_ENTITY.VERSION
        ).values(
            DATABASE_INSTALLABLE_NAME,
            SCHEMA_VERSION
        ).execute()
    }

    // Create the DAOs
    val languageDao = LanguageDao(dsl)
    val resourceMetadataDao = ResourceMetadataDao(dsl)
    val collectionDao = CollectionDao(dsl)
    val contentTypeDao = ContentTypeDao(dsl)
    val contentDao = ContentDao(dsl, contentTypeDao)
    val resourceLinkDao = ResourceLinkDao(dsl)
    val subtreeHasResourceDao = SubtreeHasResourceDao(dsl)
    val takeDao = TakeDao(dsl)
    val markerDao = MarkerDao(dsl)
    val audioPluginDao = AudioPluginDao(dsl)
    val preferenceDao = PreferenceDao(dsl)
    val installedEntityDao = InstalledEntityDao(dsl)
    val translationDao = TranslationDao(dsl)

    // Transaction support
    fun transaction(block: (DSLContext) -> Unit) {
        dsl.transaction { config ->
            // Create local transaction DSL and pass to block
            block(DSL.using(config))
        }
    }

    fun <T> transactionResult(block: (DSLContext) -> T): T {
        return dsl.transactionResult { config ->
            // Create local transaction DSL and pass to block
            block(DSL.using(config))
        }
    }
}
