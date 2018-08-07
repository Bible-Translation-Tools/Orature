package persistence

import data.dao.Dao
import data.dao.LanguageDao
import data.model.*
import data.persistence.AppDatabase
import org.jooq.Configuration
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.sqlite.SQLiteDataSource
import persistence.mapping.LanguageMapper
import persistence.mapping.UserMapper
import persistence.mapping.UserPreferencesMapper
import persistence.repo.LanguageRepo
import persistence.repo.UserLanguageRepo
import persistence.repo.UserRepo
import jooq.tables.daos.UserPreferencesEntityDao
import java.io.File
import java.nio.file.FileSystems

object AppDatabaseImpl : AppDatabase {
    private val config: Configuration
    // changed names to repo to distinguish our DAOS from generated
    private val languageRepo: LanguageDao
    private val userLanguageRepo: UserLanguageRepo
    private val userRepo: Dao<User>
    private val languageMapper: LanguageMapper
    private val userMapper: UserMapper
    private val userPreferencesMapper: UserPreferencesMapper

    init {
        Class.forName("org.sqlite.JDBC")

        val sqLiteDataSource = SQLiteDataSource()
        sqLiteDataSource.url = "jdbc:sqlite:${DirectoryProvider("8woc2018")
                .getAppDataDirectory()}${FileSystems.getDefault().separator}content.sqlite"
        sqLiteDataSource.config.toProperties().setProperty("foreign_keys", "true")
        config = DSL.using(sqLiteDataSource, SQLDialect.SQLITE).configuration()
        val file = File("src${File.separator}main${File.separator}Resources${File.separator}createAppDb.sql")
        var sql = StringBuffer()
        file.forEachLine {
            sql.append(it)
            if (it.contains(";")) {
                config.dsl().fetch(sql.toString())
                sql.delete(0, sql.length)
            }
        }

        languageMapper = LanguageMapper()
        languageRepo = LanguageRepo(config, languageMapper)
        userPreferencesMapper = UserPreferencesMapper(languageRepo)
        userLanguageRepo = UserLanguageRepo(config)
        userMapper = UserMapper(userLanguageRepo, languageRepo, UserPreferencesEntityDao(config))
        userRepo = UserRepo(config, userMapper, userPreferencesMapper)
    }

    override fun getUserDao() = userRepo

    override fun getLanguageDao() = languageRepo

    override fun getAnthologyDao(): Dao<Anthology> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getBookDao(): Dao<Book> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChapterDao(): Dao<Chapter> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChunkDao(): Dao<Chunk> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProjectDao(): Dao<Project> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getTakesDao(): Dao<Take> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}