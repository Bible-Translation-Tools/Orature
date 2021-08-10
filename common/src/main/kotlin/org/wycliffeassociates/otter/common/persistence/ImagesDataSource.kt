package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

/*
 * Chain of Responsibility - Base handler class
 */
abstract class ImagesDataSource {
    abstract fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String
    ): File?
}
