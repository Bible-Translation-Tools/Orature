/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.markerapp.app.view

import javafx.scene.layout.Priority
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.device.audio.AudioBufferPlayer
import org.wycliffeassociates.otter.jvm.device.audio.AudioDevice
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.sound.sampled.Mixer

class SourceAudioFragment : Fragment() {

    private val audioDevice = AudioDevice()
    override val root = initializeSourceContent()

    private fun initializeSourceContent(): SourceContent {
        var sourceFile: File? = null
        var startFrame: Int? = null
        var endFrame: Int? = null
        var sourceText: String? = null
        var sourceContentTitle: String? = null
        var ouputDevice: Mixer.Info? = null

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
                sourceText = parameters.named["source_text"]

                sourceContentTitle = getSourceContentTitle(
                    parameters.named["book"],
                    parameters.named["chapter_number"],
                    parameters.named["unit_number"]
                )

                ouputDevice = audioDevice.getOutputDevice(
                    parameters.named["output_device"]
                ).blockingGet()
            }
        }

        val player = sourceFile?.let {
            initializeAudioPlayer(it, startFrame, endFrame, ouputDevice)
        }

        return SourceContent().apply {
            vgrow = Priority.ALWAYS

            audioPlayerProperty.set(player)
            sourceTextProperty.set(sourceText)

            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            playLabelProperty.set(messages["playSource"])
            pauseLabelProperty.set(messages["pauseSource"])

            contentTitleProperty.set(sourceContentTitle)
            enableAudioProperty.set(false)
        }
    }

    private fun initializeAudioPlayer(
        file: File,
        start: Int?,
        end: Int?,
        audioDevice: Mixer.Info?
    ): IAudioPlayer? {
        val player = AudioBufferPlayer(audioDevice)
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

    private fun getSourceContentTitle(book: String?, chapter: String?, chunk: String?): String? {
        return if (book != null && chapter != null) {
            if (chunk != null) {
                MessageFormat.format(
                    messages["bookChapterChunkTitle"],
                    book,
                    chapter,
                    chunk
                )
            } else {
                MessageFormat.format(
                    messages["bookChapterTitle"],
                    book,
                    chapter
                )
            }
        } else {
            null
        }
    }
}
