package org.wycliffeassociates.otter.common.persistence

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

class CustomImagesDataSource : ImagesDataSource() {
    override fun getImage(metadata: ResourceMetadata, projectSlug: String): File? {
        return nextDataSource?.getImage(metadata, projectSlug)
    }
}