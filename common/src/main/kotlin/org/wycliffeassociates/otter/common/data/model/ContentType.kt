package org.wycliffeassociates.otter.common.data.model

import java.util.EnumSet

/** The kinds of [Content] elements */
enum class ContentType {
    /** ContentType of primary [Content] elements. */
    TEXT,

    /**
     * ContentType of summations or secondary representations of other [Content]s, for example the complete rendering of
     * a chapter that is primarily represented as individual verses.
     */
    META,

    /**
     * The ContentType for titles within a reference or supplemental work, typically paired with (and followed by) a
     * single element of type [BODY].
     */
    TITLE,

    /**
     * The ContentType for detail within a reference or supplemental work, always (so far) paired with (preceded by)
     * one or more elements of type [TITLE].
     */
    BODY;

    companion object {
        fun of(s: String): ContentType? {
            val lower = s.toLowerCase()
            return values().firstOrNull { lower == it.name.toLowerCase() }
        }
    }
}

/** The set of ContentTypes for primary content. Only contains [ContentType.TEXT]. */
val primaryContentTypes: Set<ContentType> = EnumSet.of(ContentType.TEXT)

/**
 * The set of ContentTypes for content that is itself a reference or resource to other content. Contains
 * [ContentType.TITLE] and [ContentType.BODY].
 */
val helpContentTypes: Set<ContentType> = EnumSet.of(ContentType.TITLE, ContentType.BODY)
