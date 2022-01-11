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
package org.wycliffeassociates.otter.jvm.markerapp.app

import com.sun.javafx.application.ParametersImpl
import javafx.application.Platform
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.FX
import tornadofx.launch

/**
 * @param args array of cli arguments
 * options include:
 *
 * --wav = the filepath to write to
 * --language = the language name to display
 * --book = the book name to display
 * --chapter = the text for "Chapter"
 * --chapter_number = the chapter number
 * --unit = the text for either "Verse" or "Chunk"
 * --unit_number = the unit number
 * --resource = the resource
 * --source_text = source text
 * --marker_total = maximum marker count to place
 * --action_title = title of the app, or action description, ie "Verse Markers" or "Place Verse Markers"
 * --content_title = title of the content opened, ie "Genesis Chapter 01"
 */
fun main(args: Array<String>) {
    FX.defaultScope = ParameterizedScope(ParametersImpl(args), { Platform.exit() })
    launch<VerseMarkerApp>(args)
}
