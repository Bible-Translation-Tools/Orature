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
    fun onNotifyProgress(localizeKey: String? = null, message: String? = null)

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