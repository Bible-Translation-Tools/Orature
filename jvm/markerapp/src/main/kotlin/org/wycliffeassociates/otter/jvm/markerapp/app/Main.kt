package org.wycliffeassociates.otter.jvm.markerapp.app

import com.sun.javafx.application.ParametersImpl
import javafx.application.Application
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.FX
import tornadofx.launch

/**
 * @param args array of cli arguments
 * options include:
 *
 * --wav= the filepath to write to
 * --language= the language name to display
 * --book= the book name to display
 * --chapter= the text for "Chapter"
 * --chapter_number= the chapter number
 * --unit= the text for either "Verse" or "Chunk"
 * --unit_number= the unit number
 * --resource= the resource
 * --source_text= source text
 */
fun main(args: Array<String>) {
    FX.defaultScope = ParameterizedScope(ParametersImpl(args),{})
    launch<VerseMarkerApp>(args)
}
