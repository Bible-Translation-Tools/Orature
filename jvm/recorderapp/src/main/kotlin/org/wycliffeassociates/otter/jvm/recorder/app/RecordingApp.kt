package org.wycliffeassociates.otter.jvm.recorder.app

import org.wycliffeassociates.otter.jvm.recorder.app.view.RecorderView
import tornadofx.App
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
 *
 * All arguments are optional. By default the recording will be written to recording.wav and the
 * display text will be the filename.
 *
 */
fun main(args: Array<String>) {
    val arguments = if (args.isNotEmpty()) args else arrayOf("--wav=recording.wav")
    launch<RecordingApp>(arguments)
}

class RecordingApp : App(RecorderView::class)