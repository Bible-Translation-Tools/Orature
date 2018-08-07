package usecases

import api.Door43Client
import api.model.Door43Mapper
import persistence.injection.DaggerPersistenceComponent

class InitializeLanguageDatabaseUseCase {

    private val languageDao = DaggerPersistenceComponent
            .builder()
            .build()
            .injectDatabase()
            .getLanguageDao()

    private val door43Client = Door43Client()

    private val door43Mapper = Door43Mapper

    // inserts languages from Door43 into the database
    fun getAndInsertLanguages() {
        door43Client.getAllLanguages().map {
            it.forEach {
                val language = door43Mapper.mapToLanguage(it)
                languageDao.insert(language)
            }
        }
    }

}