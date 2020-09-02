package org.wycliffeassociates.otter.jvm.markerapp.app

import com.sun.javafx.application.ParametersImpl
import javafx.application.Application
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
