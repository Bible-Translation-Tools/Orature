package org.wycliffeassociates.otter.jvm.workbookapp.ui.model

enum class ChunkingStep(val titleKey: String) {
    CONSUME_AND_VERBALIZE("consume_and_verbalize"),
    CHUNKING("chunking"),
    BLIND_DRAFT("blind_draft"),
    PEER_EDIT("peer_edit"),
    KEYWORD_CHECK("keyword_check"),
    VERSE_CHECK("verse_check")
}