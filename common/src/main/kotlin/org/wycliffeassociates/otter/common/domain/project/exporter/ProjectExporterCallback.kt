package org.wycliffeassociates.otter.common.domain.project.exporter

import org.wycliffeassociates.otter.common.data.primitives.Collection
import java.io.File

interface ProjectExporterCallback {
    /**
     * Pushes a success notification to the GUI.
     *
     * @param project the project that was exported
     * @param file the file or directory of the exported content
     */
    fun onNotifySuccess(project: Collection, file: File)

    /**
     * Pushes an error notification to the GUI.
     *
     * @param project the project that was exported
     */
    fun onNotifyError(project: Collection)
}