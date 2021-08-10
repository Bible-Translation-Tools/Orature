package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

class ResourceContainerImagesDataSource : ImagesDataSource() {
    override fun getImage(metadata: ResourceMetadata, projectSlug: String): File? {
        // TODO: get image from custom rc
        return null
    }
}
