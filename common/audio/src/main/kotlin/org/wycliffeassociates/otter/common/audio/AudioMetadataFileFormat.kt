/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.audio

enum class AudioMetadataFileFormat(val extension: String) {
    CUE("cue");
    companion object {
        val extensions: List<String> = values().map { it.extension }
        private val map = values().associateBy { it.extension.lowercase() }

        /** @throws IllegalArgumentException */
        fun of(extension: String) =
            map[extension.lowercase()]
                ?: throw IllegalArgumentException("Audio extension $extension not supported")

        fun isSupported(extension: String) = extension.lowercase() in extensions
    }
}
