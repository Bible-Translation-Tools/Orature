package org.wycliffeassociates.otter.jvm.workbookapp

import org.wycliffeassociates.otter.common.OratureInfo
import org.wycliffeassociates.otter.jvm.workbookapp.logging.ConfigureLogger
import org.wycliffeassociates.otter.jvm.workbookapp.persistence.DirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import tornadofx.launch

fun main(args: Array<String>) {
    initLogger()
    launch<OtterApp>(args)
}

fun initLogger() {
    val dirProv = DirectoryProvider(OratureInfo.SUITE_NAME)
    ConfigureLogger(dirProv.logsDirectory).configure()
}
