package org.wycliffeassociates.otter.common.data.primitives

/**
 * Container type enum for [ResourceMetadata.type].
 *
 * See Resource Container [Container Types](https://resource-container.readthedocs.io/en/latest/container_types.html)
 */
enum class ContainerType(val slug: String) {
    Bundle("bundle"),
    Book("book"),
    Help("help"),

    @Deprecated("Type not supported")
    Dictionary("dict"),
    @Deprecated("Type not supported")
    Manual("man");

    companion object {
        private val map = values().associateBy { it.slug.toLowerCase() }

        /** @throws IllegalArgumentException */
        fun of(slug: String) =
            map[slug.toLowerCase()]
                ?: throw IllegalArgumentException("Container slug $slug not supported")
    }
}
