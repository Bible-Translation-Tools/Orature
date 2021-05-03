//package org.wycliffeassociates.otter.jvm.workbookapp.persistence
//
//import org.jooq.Configuration
//import org.jooq.SQLDialect
//import org.jooq.impl.DSL
//import org.sqlite.SQLiteDataSource
//import java.io.File
//
//object JooqTestConfiguration {
//    init {
//        Class.forName("org.sqlite.JDBC")
//    }
//
//    private fun getConfig(databasePath: String): Configuration {
//        val sqLiteDataSource = SQLiteDataSource()
//        sqLiteDataSource.url = "jdbc:sqlite:$databasePath"
//        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")
//        val config = DSL.using(sqLiteDataSource, SQLDialect.SQLITE).configuration()
//        return config
//    }
//
//    fun createDatabase(databasePath: String, schemaFile: File): Configuration {
//        println("Creating $databasePath")
//        val config = getConfig(databasePath)
//        val sql = StringBuffer()
//        schemaFile.forEachLine {
//            sql.append(it)
//            if (it.contains(";")) {
//                config.dsl().fetch(sql.toString())
//                sql.delete(0, sql.length)
//            }
//        }
//        return config
//    }
//
//    fun connectToExistingDatabase(databasePath: String): Configuration {
//        return getConfig(databasePath)
//    }
//
//    fun deleteDatabase(databasePath: String) {
//        // delete existing database
//        val dbFile = File(databasePath)
//        if (dbFile.exists()) {
//            println("Deleting $databasePath")
//            dbFile.delete()
//        }
//    }
//}
