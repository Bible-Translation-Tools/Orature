package org.wycliffeassociates.otter.jvm.app.ui.inject

object Injector {
    val projectDao = DaggerDBComponent().build.injectProjectDao()
}