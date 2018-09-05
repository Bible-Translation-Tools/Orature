package org.wycliffeassociates.otter.jvm.app.ui.inject

import org.wycliffeassociates.otter.jvm.persistence.injection.DaggerPersistenceComponent

object Injector {
    val projectDao = DaggerPersistenceComponent
            .builder()
            .build()
            .injectDatabase()
            .getProjectDao()
}