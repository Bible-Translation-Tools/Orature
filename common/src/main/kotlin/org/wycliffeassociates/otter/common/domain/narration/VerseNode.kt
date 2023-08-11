package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.VerseMarker

data class VerseNode(
    /**
     * Start location in frames
     */
    var start: Int,
    /**
     * End location in frames
     */
    var end: Int,
    val marker: VerseMarker
)