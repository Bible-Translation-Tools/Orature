package org.wycliffeassociates.otter.jvm.workbookapp.ui.system

import java.util.*
import org.wycliffeassociates.otter.jvm.device.system.Environment

class AppInfo : Environment() {
    override fun getSystemData(): List<Pair<String, String>> {
        return listOf(
            Pair("os name", System.getProperty("os.name")),
            Pair("os version", System.getProperty("os.version")),
            Pair("os architecture", System.getProperty("os.arch")),
            Pair("java version", System.getProperty("java.version"))
        )
    }

    override fun getVersion(): String? {
        val prop = Properties()
        val inputStream = javaClass.classLoader.getResourceAsStream("version.properties")

        if (inputStream != null) {
            prop.load(inputStream)
            return prop.getProperty("version")
        }

        return null
    }
}
