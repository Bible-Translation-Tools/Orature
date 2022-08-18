package org.wycliffeassociates.otter.jvm.workbookapp.persistence.database

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.nio.file.Files

const val DB_FILE_NAME = "app_db_sqlite"
const val OLD_DB_FILE_NAME = "content.sqlite"

class DatabaseInitializer(
    private val directoryProvider: IDirectoryProvider
) {

    fun initialize() {
        val databaseFile = directoryProvider.databaseDirectory
            .resolve(DB_FILE_NAME)
        val oldDbFile = directoryProvider.getUserDataDirectory()
            .resolve(OLD_DB_FILE_NAME)
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
                val existingDbVersion = AppDatabase.getDatabaseVersion(databaseFile)
                if (existingDbVersion > SCHEMA_VERSION) {
                    archiveDb(databaseFile)
                }
            }
        }
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
}