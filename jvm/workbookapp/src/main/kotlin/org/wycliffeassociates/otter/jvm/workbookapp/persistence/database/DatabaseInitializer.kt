/**
 * Copyright (C) 2020-2023 Wycliffe Associates
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

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.nio.file.Files

const val DB_FILE_NAME = "app_db_sqlite"
const val OLD_DB_FILE_NAME = "content.sqlite"

class DatabaseInitializer(
    private val directoryProvider: IDirectoryProvider,
) {
    fun initialize() {
        val databaseFile =
            directoryProvider.databaseDirectory
                .resolve(DB_FILE_NAME)
        val oldDbFile =
            directoryProvider.getAppDataDirectory()
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
                if (existingDbVersion == null || existingDbVersion > SCHEMA_VERSION) {
                    archiveDb(databaseFile)
                }
            }
        }
    }

    private fun archiveDb(dbFile: File) {
        val archive =
            directoryProvider.databaseDirectory
                .resolve("archive")
                .let {
                    it.mkdirs()
                    it.resolve(
                        "app_db-${System.currentTimeMillis()}.sqlite",
                    )
                }

        Files.move(
            dbFile.toPath(),
            archive.toPath(),
        )
    }
}
