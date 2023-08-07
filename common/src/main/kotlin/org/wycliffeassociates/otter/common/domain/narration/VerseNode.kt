package org.wycliffeassociates.otter.common.domain.narration

data class VerseNode(
    /**
     * Start location in frames
     */
    var start: Int,
    /**
     * End location in frames
     */
    var end: Int
)