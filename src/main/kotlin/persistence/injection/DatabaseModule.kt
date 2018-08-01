package persistence.injection

import dagger.Module
import dagger.Provides
import data.persistence.AppDatabase
import persistence.AppDatabaseImpl

@Module
class DatabaseModule {
    @Provides
    fun providesAppDatabase() : AppDatabase = AppDatabaseImpl
}