package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

class DefaultImagesDataSource : ImagesDataSource() {
    override fun getImage(metadata: ResourceMetadata, projectSlug: String): File? {
        // TODO: use default artwork from static resources
        return File("""\default\artwork\path""")
    }
}