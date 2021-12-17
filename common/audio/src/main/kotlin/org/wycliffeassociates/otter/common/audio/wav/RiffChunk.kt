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
package org.wycliffeassociates.otter.common.audio.wav

import java.nio.ByteBuffer

internal const val CHUNK_HEADER_SIZE = 8
internal const val CHUNK_LABEL_SIZE = 4
internal const val DWORD_SIZE = 4

// chunk data must be word aligned but the size might not account for padding
// therefore, if odd, the size we read must add one to include the padding
// https://sharkysoft.com/jwave/docs/javadocs/lava/riff/wave/doc-files/riffwave-frameset.htm
internal fun wordAlign(subchunkSize: Int) = subchunkSize + if (subchunkSize % 2 == 0) 0 else 1

interface RiffChunk {
    fun parse(chunk: ByteBuffer)
    fun toByteArray(): ByteArray
    val totalSize: Int
}
