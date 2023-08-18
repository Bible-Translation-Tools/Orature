package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.VerseMarker

internal data class VerseNode(
    /**
     * Start location in frames
     */
    var start: Int,
    /**
     * End location in frames
     */
    var end: Int,
    var placed: Boolean = false,
    val marker: VerseMarker
) {
    fun clear() {
        start = 0
        end = 0
        placed = false
    }
}