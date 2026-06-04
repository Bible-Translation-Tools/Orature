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
package org.wycliffeassociates.otter.common.domain.brightcove

import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import java.io.File
import javax.inject.Inject

data class VerseTiming(
    val markers: List<VerseMarker>,
    val totalFrames: Int
)

interface VerseTimingProvider {
    fun getTiming(audioFile: File): VerseTiming
}

class OratureVerseTimingProvider @Inject constructor() : VerseTimingProvider {
    override fun getTiming(audioFile: File): VerseTiming {
        val oratureAudio = OratureAudioFile(audioFile)
        val markers = oratureAudio.getMarker<VerseMarker>()
            .sortedBy { it.location }
        return VerseTiming(markers, oratureAudio.totalFrames)
    }
}
