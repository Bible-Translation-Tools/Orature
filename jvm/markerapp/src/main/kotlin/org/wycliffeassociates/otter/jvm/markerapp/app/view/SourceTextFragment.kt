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

import org.wycliffeassociates.otter.jvm.controls.media.SourceContent
import org.wycliffeassociates.otter.jvm.workbookplugin.plugin.ParameterizedScope
import tornadofx.*
import java.text.MessageFormat

class SourceTextFragment : Fragment() {

//    @Inject
//    lateinit var player: IAudioPlayer

    override val root = initializeSourceContent()

    private fun initializeSourceContent(): SourceContent {

        var sourceText: String? = null
        var sourceContentTitle: String? = null

        if (scope is ParameterizedScope) {
            val parameters = (scope as? ParameterizedScope)?.parameters

            parameters?.let {
                sourceText = parameters.named["source_text"]

                sourceContentTitle = getSourceContentTitle(
                    parameters.named["book"],
                    parameters.named["chapter_number"],
                    parameters.named["unit_number"]
                )
            }
        }

        return SourceContent().apply {
            sourceTextProperty.set(sourceText)
            textNotAvailableTextProperty.set(messages["textNotAvailable"])
            contentTitleProperty.set(sourceContentTitle)

            enableAudioProperty.set(false)
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
