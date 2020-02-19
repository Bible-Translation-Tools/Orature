package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.wycliffeassociates.otter.jvm.controls.AudioPlayer
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.Fragment
import java.io.File

class SourceAudioFragment : Fragment() {

    override val root = initializeAudioPlayer()

    private fun initializeAudioPlayer(): AudioPlayer {
        var sourceFile: File? = null
        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                val sourceAudio: String? = parameters.named["chapter_audio"]
                sourceFile = if (sourceAudio != null && File(sourceAudio).exists()) File(sourceAudio) else null
            }
        }
        return AudioPlayer(sourceFile)
    }
}