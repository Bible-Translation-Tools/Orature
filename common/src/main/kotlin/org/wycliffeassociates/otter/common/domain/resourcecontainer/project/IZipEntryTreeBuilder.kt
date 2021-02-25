package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import org.wycliffeassociates.otter.common.collections.OtterTree
import java.util.zip.ZipFile

interface IZipEntryTreeBuilder {
    fun buildOtterFileTree(zipFile: ZipFile, projectPath: String, rootPathWithinZip: String?): OtterTree<OtterFile>
}
