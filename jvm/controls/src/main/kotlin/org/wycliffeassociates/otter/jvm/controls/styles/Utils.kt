/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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

/**
 * Wrapper of FX.importStylesheet(String).
 * Use this function to prevent duplicated imports.
 */
fun tryImportStylesheet(stylesheet: String) : Boolean {
    val css = stylesheet.replace(" ", "%20")
    try {
        URL(css).toExternalForm()
    } catch (ex: MalformedURLException) {
        // Class.getResource is scoped to a single module under JPMS, so it cannot see
        // stylesheets owned by other modules; the class loader search covers every open
        // module on the module path as well as the class path.
        FX::class.java.getResource(css)?.toExternalForm()
            ?: object {}.javaClass.classLoader.getResource(css.trimStart('/'))?.toExternalForm()
    }?.let { resourcePath ->
        if (!FX.stylesheets.contains(resourcePath)) {
            importStylesheet(resourcePath)
            return true
        }
    }
    return false
}
