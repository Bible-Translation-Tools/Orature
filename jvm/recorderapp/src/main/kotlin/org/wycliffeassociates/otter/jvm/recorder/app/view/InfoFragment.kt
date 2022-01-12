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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*

class InfoFragment : Fragment() {

    val languageIcon = FontIcon("gmi-favorite")
    val bookIcon = FontIcon("fas-book")
    val chapterIcon = FontIcon("gmi-book")
    val unitIcon = FontIcon("gmi-bookmark-border")
    val fileIcon = FontIcon("gmi-insert-drive-file")
    val resourceIcon = FontIcon("gmi-forum")

    override val root = hbox {
        addClass("info")
    }

    init {
        addRecordingInfoFromParams()
    }

    private fun addRecordingInfoFromParams() {

        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                val language = parameters.named["language"]
                val book = parameters.named["book"]
                val chapter = parameters.named["chapter"]
                val cnum = parameters.named["chapter_number"]
                val unit = parameters.named["unit"]
                val unum = parameters.named["unit_number"]
                val resource = parameters.named["resource"]

                language?.let {
                    root.add(InfoItem(it, null, languageIcon))
                }
                book?.let {
                    root.add(InfoItem(it, null, bookIcon))
                }
                chapter?.let {
                    root.add(InfoItem(it, cnum, chapterIcon))
                }
                unit?.let {
                    root.add(InfoItem(it, unum, unitIcon))
                }
                resource?.let {
                    root.add(InfoItem(it, null, resourceIcon))
                }

                if (arrayOf(language, book, chapter, unit).all { it == null }) {
                    val wav = app.parameters.named["wav"]
                    wav?.let {
                        root.add(InfoItem(it, null, fileIcon))
                    }
                }
            }
        }
    }
}
