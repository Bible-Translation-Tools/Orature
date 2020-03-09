package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.AudioPlayerNode
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.Fragment
import java.io.File
import java.lang.Exception

class SourceAudioFragment : Fragment() {

    override val root = initializeAudioNode()

    private fun initializeAudioNode(): AudioPlayerNode {
        var sourceFile: File? = null
        var startFrame: Int? = null
        var endFrame: Int? = null
        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                val sourceAudio: String? = parameters.named["chapter_audio"]
                sourceFile = if (sourceAudio != null && File(sourceAudio).exists()) File(sourceAudio) else null
                startFrame = parameters.named["source_chunk_start"]?.toInt()
                endFrame = parameters.named["source_chunk_end"]?.toInt()
            }
        }
        val player = sourceFile?.let { initializeAudioPlayer(it, startFrame, endFrame) }
        return AudioPlayerNode(player)
    }

    private fun initializeAudioPlayer(file: File, start: Int?, end: Int?): IAudioPlayer? {
        val player = AudioBufferPlayer()
        return try {
            if (start != null && end != null) {
                player.loadSection(file, start, end)
            } else {
                player.load(file)
            }
            player
        } catch (e: Exception) {
            null
        }
    }
}