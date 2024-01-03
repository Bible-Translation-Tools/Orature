/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.ObservableEmitter
import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.data.ProgressStatus
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor

/**
 * Sets up the callback for pushing progress status to the UI
 *
 * @param progressStatusEmitter the interface to emit status info to the channel
 */
internal fun setupImportCallback(
    progressStatusEmitter: ObservableEmitter<ProgressStatus>
): ProjectImporterCallback {
    return object : ProjectImporterCallback {
        override fun onRequestUserInput(): Single<ImportOptions> {
            throw NotImplementedError("no op")
        }

        override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
            throw NotImplementedError("no op")
        }

        override fun onNotifySuccess(language: String?, project: String?, workbookDescriptor: WorkbookDescriptor?) {
            /* no-op */
        }

        override fun onError(filePath: String) {
            /* no-op */
        }

        override fun onNotifyProgress(localizeKey: String?, message: String?, percent: Double?) {
            localizeKey?.let {
                progressStatusEmitter.onNext(
                    ProgressStatus(subTitleKey = localizeKey, subTitleMessage = message)
                )
            }
        }
    }
}