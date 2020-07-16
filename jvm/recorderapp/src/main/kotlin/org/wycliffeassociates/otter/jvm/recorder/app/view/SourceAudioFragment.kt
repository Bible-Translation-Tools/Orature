package org.wycliffeassociates.otter.jvm.recorder.app.view

import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.sourcecontent.SourceContent
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Exception

class SourceAudioFragment : Fragment() {

    override val root = initializeSourceContent()

    private fun initializeSourceContent(): SourceContent {
        var sourceFile: File? = null
        var startFrame: Int? = null
        var endFrame: Int? = null
        var sourceText: String? = null
        var bookTitle: String? = null
        var chapterTitle: String? = null
        var chunkTitle: String? = null

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

                bookTitle = parameters.named["book"]
                chapterTitle = parameters.named["chapter_number"]
                chunkTitle = parameters.named["unit_number"]
            }
        }

        val player = sourceFile?.let { initializeAudioPlayer(it, startFrame, endFrame) }

        return SourceContent().apply {
            audioPlayerProperty.set(player)
            sourceTextProperty.set(sourceText)

            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])

            bookTitleProperty.set(bookTitle)
            chapterTitleProperty.set(chapterTitle)
            chunkTitleProperty.set(chunkTitle)

            playLabelProperty.set(messages["playSource"])
            pauseLabelProperty.set(messages["pauseSource"])
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
