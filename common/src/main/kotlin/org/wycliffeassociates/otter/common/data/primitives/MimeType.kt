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
package org.wycliffeassociates.otter.common.data.primitives

import java.lang.IllegalArgumentException

enum class MimeType(vararg types: String) {
    USFM("text/usfm", "text/x-usfm", "text/usfm3", "usfm"),
    MARKDOWN("text/markdown", "text/x-markdown", "markdown"),
    WAV("audio/wav", "audio/wave", "audio/x-wave", "audio/vnd.wave");

    val accepted = types.toList()
    val norm = accepted.first()

    companion object {
        private val map: Map<String, MimeType> = values()
            .flatMap { mt -> mt.accepted.map { it to mt } }
            .associate { it }

        /** @throws [IllegalArgumentException] if the format type is not supported **/
        fun of(type: String) = map[type.toLowerCase()]
            ?: throw IllegalArgumentException("Mime type $type not supported")
    }
}
