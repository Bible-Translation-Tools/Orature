package org.wycliffeassociates.otter.common.data.primitives

enum class ContentLabel(val value: String, val type: ContentType) {
    CHAPTER("chapter", ContentType.META),
    VERSE("chunk", ContentType.TEXT),
    HELP_TITLE("title", ContentType.TITLE),
    HELP_BODY("body", ContentType.BODY);

    companion object {
        fun of(contentType: ContentType) = when (contentType) {
            ContentType.META -> CHAPTER
            ContentType.TEXT -> VERSE
            ContentType.TITLE -> HELP_TITLE
            ContentType.BODY -> HELP_BODY
        }
    }
}
