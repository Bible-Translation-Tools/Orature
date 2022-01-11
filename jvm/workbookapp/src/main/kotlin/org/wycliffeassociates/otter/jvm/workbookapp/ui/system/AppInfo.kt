/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.workbookapp.ui.system

import org.wycliffeassociates.otter.jvm.device.system.Environment
import java.util.*

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
