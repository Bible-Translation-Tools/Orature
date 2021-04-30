package org.wycliffeassociates.otter.jvm.workbookapp

import org.wycliffeassociates.otter.jvm.workbookapp.di.DaggerAppDependencyGraph
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import tornadofx.launch

fun main(args: Array<String>) {
    val dependencyGraph = DaggerAppDependencyGraph.builder().build()
    val dp = dependencyGraph.injectDirectoryProvider()
    ConfigureLogger(
        dp.logsDirectory
    ).configure()
    launch<OtterApp>(args)
}
