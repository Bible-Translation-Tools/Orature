package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.wycliffeassociates.otter.jvm.controls.AudioPlayer
import tornadofx.Fragment
import java.io.File

class SourceAudioFragment : Fragment() {

    private val sourceAudio: String? = workspace.params["source"] as String?
    private val sourceFile = if (sourceAudio != null && File(sourceAudio).exists()) File(sourceAudio) else null

    override val root = AudioPlayer(sourceFile)
}