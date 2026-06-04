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
import org.wycliffeassociates.otter.common.domain.audio.metadata.framesToUs

class BrightcoveCuePointBuilder {

    fun buildCuePoints(
        bookCode: String,
        chapterNumber: Int,
        verseMarkers: List<VerseMarker>
    ): List<BrightcoveCuePoint> {
        if (verseMarkers.isEmpty()) {
            return emptyList()
        }

        return verseMarkers
            .sortedBy { it.location }
            .map { marker ->
                val startUs = framesToUs(marker.location)
                val seconds = startUs / 1_000_000.0
                BrightcoveCuePoint(
                    name = formatVerseReference(bookCode, chapterNumber, marker.start, marker.end),
                    time = seconds
                )
            }
    }
}
