package org.wycliffeassociates.otter.common.data.model

/**
 * Container type enum for [ResourceMetadata.type].
 *
 * See Resource Container [Container Types](https://resource-container.readthedocs.io/en/latest/container_types.html)
 */
enum class ContainerType(val slug: String) {
    Book("book"),
    Help("help"),
    Dictionary("dict"),
    Manual("man"),
    Bundle("bundle");

    companion object {
        private val map = values().associateBy { it.slug.toLowerCase() }

        /** @throws IllegalArgumentException */
        fun of(slug: String) =
            map[slug.toLowerCase()]
                ?: throw IllegalArgumentException("Container slug $slug not supported")
    }
}
