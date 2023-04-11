package org.wycliffeassociates.otter.assets.initialization

import io.reactivex.Observer
import io.reactivex.Single
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.persistence.config.ProgressStatus

internal fun setupImportCallback(
    progressStatusEmitter: Observer<ProgressStatus>
): ProjectImporterCallback {
    return object : ProjectImporterCallback {
        override fun onRequestUserInput(): Single<ImportOptions> {
            TODO("Not yet implemented")
        }

        override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
            TODO("Not yet implemented")
        }

        override fun onError(messageKey: String) {
            TODO("Not yet implemented")
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