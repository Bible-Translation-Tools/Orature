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
package org.wycliffeassociates.otter.common.domain.plugins

import java.io.File

data class PluginParameters(
    val languageName: String,
    val bookTitle: String,
    val chapterLabel: String,
    val chapterNumber: Int,
    val verseTotal: Int?,
    val chunkLabel: String? = null,
    val chunkNumber: Int? = null,
    val resourceLabel: String? = null,
    val sourceChapterAudio: File? = null,
    val sourceChunkStart: Int? = null,
    val sourceChunkEnd: Int? = null,
    val sourceText: String? = null,
    val actionText: String = "",
    val targetChapterAudio: File? = null
)
