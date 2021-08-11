package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

interface ImagesDataSource {
    fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio
    ): File?


    fun getImagePathWithRatio(
        path: String,
        ratio: ImageRatio
    ): String {
        val image = File(path)
        val nameWithRatio =
            image.nameWithoutExtension + ratio.getStringFormat()

        return image.parentFile.resolve(nameWithRatio)
            .invariantSeparatorsPath
    }
}
