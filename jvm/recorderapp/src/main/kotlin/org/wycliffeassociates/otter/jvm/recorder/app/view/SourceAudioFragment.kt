package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.SourceContent
import org.wycliffeassociates.otter.jvm.controls.skins.media.CompactSourceContentSkin
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Exception

class SourceAudioFragment : Fragment() {

    override val root = initializeSourceContent()

    init {
        root.apply {
            style {
                skin = CompactSourceContentSkin::class
            }
            sourceTextWidthProperty.bind(widthProperty().divide(2))
        }
    }

    private fun initializeSourceContent(): SourceContent {
        var sourceFile: File? = null
        var startFrame: Int? = null
        var endFrame: Int? = null
        var sourceText: String? = null
        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                val sourceAudio: String? = parameters.named["chapter_audio"]
                sourceFile = if (sourceAudio != null && File(sourceAudio).exists()) File(sourceAudio) else null
                try {
                    startFrame = parameters.named["source_chunk_start"]?.toInt()
                    endFrame = parameters.named["source_chunk_end"]?.toInt()
                } catch (e: NumberFormatException) {
                    startFrame = null
                    endFrame = null
                }
                sourceText = parameters.named["source_text"] ?: ""
            }
        }

        val player = sourceFile?.let { initializeAudioPlayer(it, startFrame, endFrame) }

        return SourceContent().apply {
            audioPlayerProperty.set(player)
            sourceTextProperty.set(sourceText)
        }
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