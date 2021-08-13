package org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork

import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import java.io.File

interface ImagesDataSource {
    fun getImage(
        metadata: ResourceMetadata,
        projectSlug: String,
        imageRatio: ImageRatio = ImageRatio.DEFAULT
    ): File?


    fun getImagePathWithRatio(
        path: String,
        ratio: ImageRatio
    ): String {
        if (ratio == ImageRatio.DEFAULT) {
            return path
        }
        val image = File(path)
        val nameWithRatio =
            "${image.nameWithoutExtension}_${ratio.getStringFormat()}.${image.extension}"

        return image.parentFile.resolve(nameWithRatio)
            .invariantSeparatorsPath
    }
}
