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
package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single
import org.wycliffeassociates.otter.common.data.primitives.Collection
import org.wycliffeassociates.otter.common.data.workbook.WorkbookDescriptor

/**
 *  Defines a set of callback methods that can be used to handle callbacks
 *  related to importing a project. This interface communicates with the user.
 */
interface ProjectImporterCallback {
    /**
     * Called when the importer requires input from the user. This method
     * should be implemented by the client code.
     *
     * @return the user's input
     */
    fun onRequestUserInput(): Single<ImportOptions>
    /**
     * Requests for user input with the parameter provided.
     *
     * @param parameter presents the available options for the user.
     * @return the user's selection
     * @see ProjectImporterCallback.onRequestUserInput
     */
    fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions>

    /**
     * Sends progress status information to the listeners/handlers of this callback.
     *
     * @param localizeKey the identifier for localization string
     * @param message the value to be formatted with the given key
     */
    fun onNotifyProgress(localizeKey: String? = null, message: String? = null, percent: Double? = null)

    /**
     * Pushes a success message notification to the listeners/handlers of this callback.
     *
     * @param workbookDescriptor information about the imported project.
     * This could be used for further actions following up the callback.
     */
    fun onNotifySuccess(
        language: String? = null,
        project: String? = null,
        workbookDescriptor: WorkbookDescriptor? = null
    )

    /**
     * Called when the importer encounters an error that needs to alert the user.
     *
     * @param filePath path to the import file that caused an error while importing.
     */
    fun onError(filePath: String)
}

/**
 * The import callback options presented to the user. This will associate with a callback
 * to request user's input.
 */
data class ImportCallbackParameter(val options: List<Int>, val name: String = "")