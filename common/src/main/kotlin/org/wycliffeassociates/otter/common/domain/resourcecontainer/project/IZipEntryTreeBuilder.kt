package org.wycliffeassociates.otter.common.domain.resourcecontainer.project

import java.util.zip.ZipFile
import org.wycliffeassociates.otter.common.collections.tree.OtterTree

interface IZipEntryTreeBuilder {
    fun buildOtterFileTree(zipFile: ZipFile, projectPath: String, rootPathWithinZip: String?): OtterTree<OtterFile>
}
