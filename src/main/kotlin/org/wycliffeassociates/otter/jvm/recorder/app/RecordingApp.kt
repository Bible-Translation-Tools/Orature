package org.wycliffeassociates.otter.jvm.recorder.app

import javafx.scene.text.Font
import org.wycliffeassociates.otter.jvm.recorder.app.view.RecorderView
import tornadofx.App
import tornadofx.launch

fun main(args: Array<String>) {
    launch<RecordingApp>(
        arrayOf(
            "--wav=hello.wav",
            "--lang=English",
            "--book=Matthew",
            "--chap=Chapter",
            "--cnum=5",
            "--unit=Verse",
            "--unum=10"
        )
    )
}

class RecordingApp: App(RecorderView::class)