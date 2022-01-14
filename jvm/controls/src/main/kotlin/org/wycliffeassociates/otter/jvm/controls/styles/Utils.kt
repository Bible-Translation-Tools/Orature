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
package org.wycliffeassociates.otter.jvm.controls.styles

import tornadofx.FX
import tornadofx.importStylesheet
import java.net.MalformedURLException
import java.net.URL

fun tryImportStylesheet(stylesheet: String) : Boolean {
    try {
        URL(stylesheet).toExternalForm()
    } catch (ex: MalformedURLException) {
        // Fallback to loading classpath resource
        FX::class.java.getResource(stylesheet)?.toExternalForm()
    }?.let { resourcePath ->
        if (!FX.stylesheets.contains(resourcePath)) {
            importStylesheet(resourcePath)
            return true
        }
    }
    return false
}