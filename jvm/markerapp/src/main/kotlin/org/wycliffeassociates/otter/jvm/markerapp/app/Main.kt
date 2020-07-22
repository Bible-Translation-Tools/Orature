package org.wycliffeassociates.otter.jvm.markerapp.app

import javafx.application.Application

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
    Application.launch(VerseMarkerApp::class.java, *args)
}
