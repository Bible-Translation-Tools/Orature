package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

/*
 * Chain of Responsibility - Base handler class
 */
abstract class ImagesDataSource {
    protected var nextDataSource: ImagesDataSource? = null

    abstract fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String
    ): File?

    fun setFallbackDataSource(dataSource: ImagesDataSource) {
        nextDataSource = dataSource
    }
}