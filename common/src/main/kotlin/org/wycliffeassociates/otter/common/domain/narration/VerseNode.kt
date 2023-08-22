package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.VerseMarker

internal data class VerseNode(
    /**
     * Start location in audio frames within the scratch audio recording. This is an absolute frame position into
     * the file.
     */
    var startScratchFrame: Int,
    /**
     * End location in audio frames within the scratch audio recording. This is an absolute frame position into
     * the file.
     */
    var endScratchFrame: Int,
    var placed: Boolean = false,
    val marker: VerseMarker
) {
    fun clear() {
        startScratchFrame = 0
        endScratchFrame = 0
        placed = false
    }

    val length: Int
        get() = startScratchFrame - endScratchFrame
}