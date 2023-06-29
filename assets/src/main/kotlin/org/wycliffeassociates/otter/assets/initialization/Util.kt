package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.ObservableEmitter
import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.data.ProgressStatus

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

        override fun onNotifySuccess(languageName: String, projectSlug: String?) {
            /* no-op */
        }

        override fun onError(filePath: String) {
            /* no-op */
        }

        override fun onNotifyProgress(localizeKey: String?, message: String?) {
            localizeKey?.let {
                progressStatusEmitter.onNext(
                    ProgressStatus(subTitleKey = localizeKey, subTitleMessage = message)
                )
            }
        }
    }
}