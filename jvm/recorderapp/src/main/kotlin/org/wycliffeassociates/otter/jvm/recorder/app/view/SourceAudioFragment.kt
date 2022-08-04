/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.recorder.app.view

import javafx.geometry.NodeOrientation
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.controls.Shortcut
import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.io.File
import java.lang.Exception
import java.text.MessageFormat
import org.wycliffeassociates.otter.jvm.device.audio.AudioConnectionFactory

class SourceAudioFragment : Fragment() {

    override val root = initializeSourceContent()

    private fun initializeSourceContent(): SourceContent {

        var sourceText: String? = null
        var sourceContentTitle: String? = null
        var license: String? = null
        var direction: String? = null
        var sourceDirection: String? = null
        var sourceRate: Double? = null
        var targetRate: Double? = null

        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                sourceText = parameters.named["source_text"]
                license = parameters.named["license"]
                direction = parameters.named["direction"]
                sourceDirection = parameters.named["source_direction"]
                sourceRate = parameters.named["source_rate"]?.toDouble()
                targetRate = parameters.named["target_rate"]?.toDouble()

                sourceContentTitle = getSourceContentTitle(
                    parameters.named["book"],
                    parameters.named["chapter_number"],
                    parameters.named["unit_number"]
                )
            }
        }

        return SourceContent().apply {
            sourceTextProperty.set(sourceText)
            sourceTextCompactMode.set(true)
            audioNotAvailableTextProperty.set(messages["audioNotAvailable"])
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            playSourceLabelProperty.set(messages["playSource"])
            pauseSourceLabelProperty.set(messages["pauseSource"])
            playTargetLabelProperty.set(messages["playTarget"])
            pauseTargetLabelProperty.set(messages["pauseTarget"])
            licenseProperty.set(license)

            contentTitleProperty.set(sourceContentTitle)
            orientationProperty.set(
                when (direction) {
                    "rtl" -> NodeOrientation.RIGHT_TO_LEFT
                    else -> NodeOrientation.LEFT_TO_RIGHT
                }
            )
            sourceOrientationProperty.set(
                when (sourceDirection) {
                    "rtl" -> NodeOrientation.RIGHT_TO_LEFT
                    else -> NodeOrientation.LEFT_TO_RIGHT
                }
            )

            sourceSpeedRateProperty.set(sourceRate ?: 1.0)
            targetSpeedRateProperty.set(targetRate ?: 1.0)
        }
    }

    override fun onDock() {
        super.onDock()
        var sourceFile: File?
        var startFrame: Int?
        var endFrame: Int?
        var targetFile: File?

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
                val player = sourceFile?.let { initializeAudioPlayer(it, startFrame, endFrame) }

                root.sourceAudioPlayerProperty.set(player)
                root.sourceAudioPlayerProperty.value?.let {
                    shortcut(Shortcut.PLAY_SOURCE.value, it::toggle)
                }

                val targetAudio: String? = parameters.named["target_chapter_audio"]
                targetFile = if (targetAudio != null && File(targetAudio).exists()) File(targetAudio) else null
                val targetPlayer = targetFile?.let { initializeAudioPlayer(it) }
                root.targetAudioPlayerProperty.set(targetPlayer)
                root.targetAudioPlayerProperty.value?.let {
                    shortcut(Shortcut.PLAY_TARGET.value, it::toggle)
                }
            }
        }
    }

    private fun initializeAudioPlayer(file: File, start: Int? = null, end: Int? = null): IAudioPlayer? {
        val connectionFactory = workspace.params["audioConnectionFactory"] as AudioConnectionFactory
        val player = connectionFactory.getPlayer()
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
        return null
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
