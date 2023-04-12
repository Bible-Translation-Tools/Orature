package org.wycliffeassociates.otter.common.domain.project.importer

import io.reactivex.Single

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

    fun onNotifyProgress(localizeKey: String? = null, message: String? = null)

    /**
     * Called when the importer encounters an error that needs to alert the user.
     *
     * @param messageKey the string identifier mapped to the localized text of the message.
     */
    fun onError(messageKey: String)
}

/**
 * The import callback options presented to the user. This will associate with a callback
 * to request user's input.
 */
data class ImportCallbackParameter(val options: List<Int>)