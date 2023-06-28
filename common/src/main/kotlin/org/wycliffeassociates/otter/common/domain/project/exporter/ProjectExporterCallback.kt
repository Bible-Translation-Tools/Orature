package org.wycliffeassociates.otter.common.domain.project.exporter

import org.wycliffeassociates.otter.common.data.primitives.Collection
import java.io.File

interface ProjectExporterCallback {
    fun onNotifySuccess(project: Collection, file: File)
    fun onNotifyError(project: Collection)
}