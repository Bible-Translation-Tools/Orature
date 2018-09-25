package org.wycliffeassociates.otter.jvm.app.ui.inject

object Injector {
    private val database = DaggerPersistenceComponent
            .builder()
            .build()
            .injectDatabase()
  
    val projectDao = database.getProjectDao()
    val chapterDao =database.getChapterDao()
    val bookDao = database.getBookDao()
    val chunkDao = database.getChunkDao()
    val takesDao = database.getTakesDao()
}