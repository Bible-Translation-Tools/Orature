package org.wycliffeassociates.otter.jvm.controls.model

enum class ChunkingStep(val titleKey: String) {
    CONSUME_AND_VERBALIZE("consume_and_verbalize"),
    CHUNKING("chunking"),
    BLIND_DRAFT("blind_draft_and_self_edit"),
    PEER_EDIT("peer_edit"),
    KEYWORD_CHECK("keyword_check"),
    VERSE_CHECK("verse_check")
}