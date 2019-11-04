package org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport

import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ProjectImporter(
    private val projectAudioDirectory: File,
    private val directoryProvider: IDirectoryProvider
) {
    fun isInProgressProject(resourceContainer: File): Boolean {
        return resourceContainer.isFile &&
                resourceContainer.extension == "zip" &&
                directoryProvider.newZipFileReader(resourceContainer).exists(RcConstants.SELECTED_TAKES_FILE)
    }
}
