/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.resourcecontainer.project.markdown

import java.io.BufferedReader

// TODO: Add Help type enum to HelpResource? (tn, tq)
data class HelpResource(var title: String, var body: String)

object ParseMd {

    private val isTitleRegex = Regex("^#+\\s*[^#\\s]+")
    private val titleTextRegex = Regex("^#+\\s*")

    fun parseHelp(reader: BufferedReader): List<HelpResource> {
        val helpResourceList = ArrayList<HelpResource>()

        reader.forEachLine {
            if (it.isEmpty()) {
                return@forEachLine // continue
            }
            // If we have a title, add a new help resource to the end of the list
            if (isTitleLine(it)) {
                helpResourceList.add(HelpResource(it, ""))
            }
            // Found body text. Add the body to the help resource at the end of the list
            // If the list is empty, the body text will be discarded (this should never happen.)
            else if (helpResourceList.isNotEmpty()) {
                if (helpResourceList.last().body.isNotEmpty()) {
                    helpResourceList.last().body += System.lineSeparator()
                }
                helpResourceList.last().body += it
            }
        }
        return helpResourceList
    }

    fun parse(reader: BufferedReader): List<String> =
        reader
            .lineSequence()
            .filter { it.isNotBlank() }
            .filter { !it.matches(Regex("^!\\[.*\\]\\(.*\\)$")) }
            .toList()

    internal fun getTitleText(line: String): String? {
        titleTextRegex.find(line)?.let {
            return line.removePrefix(it.value)
        } ?: return null
    }

    internal fun isTitleLine(line: String): Boolean = isTitleRegex.containsMatchIn(line)
}
