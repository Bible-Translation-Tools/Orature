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

import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteDataSource
import java.io.File

object JooqTestConfiguration {
    init {
        Class.forName("org.sqlite.JDBC")
    }

    private fun getConfig(databasePath: String): Configuration {
        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:$databasePath"
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")
        val config = DSL.using(sqLiteDataSource, SQLDialect.SQLITE).configuration()
        return config
    }

    fun createDatabase(databasePath: String, schemaFile: File): Configuration {
        println("Creating $databasePath")
        val config = getConfig(databasePath)
        val sql = StringBuffer()
        schemaFile.forEachLine {
            sql.append(it)
            if (it.contains(";")) {
                config.dsl().fetch(sql.toString())
                sql.delete(0, sql.length)
            }
        }
        return config
    }

    fun connectToExistingDatabase(databasePath: String): Configuration {
        return getConfig(databasePath)
    }

    fun deleteDatabase(databasePath: String) {
        // delete existing database
        val dbFile = File(databasePath)
        if (dbFile.exists()) {
            println("Deleting $databasePath")
            dbFile.delete()
        }
    }
}
