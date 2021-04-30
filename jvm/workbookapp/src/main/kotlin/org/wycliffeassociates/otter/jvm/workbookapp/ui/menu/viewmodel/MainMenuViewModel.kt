package org.wycliffeassociates.otter.jvm.workbookapp.ui.menu.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import javafx.beans.property.SimpleBooleanProperty
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.persistence.repositories.IAudioPluginRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ProjectGridViewModel
import tornadofx.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class MainMenuViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(MainMenuViewModel::class.java)

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var pluginRepository: IAudioPluginRepository
    @Inject lateinit var workbookRepository: IWorkbookRepository
    @Inject lateinit var importRcProvider: Provider<ImportResourceContainer>
    @Inject lateinit var projectExporterProvider: Provider<ProjectExporter>

    val showExportDialogProperty = SimpleBooleanProperty(false)
    val showImportDialogProperty = SimpleBooleanProperty(false)

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun importResourceContainer(fileOrDir: File) {
        showImportDialogProperty.set(true)

        importRcProvider.get()
            .import(fileOrDir)
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in importing resource container $fileOrDir", e)
            }
            .subscribe { result: ImportResult ->
                if (result == ImportResult.SUCCESS) {
                    find<ProjectGridViewModel>().loadProjects()
                }

                showImportDialogProperty.value = false

                result.errorMessage?.let {
                    error(messages["importError"], it)
                }
            }
    }
}

/** Null on success, otherwise localized error text. */
val ImportResult.errorMessage: String?
    get() {
        return when (this) {
            ImportResult.SUCCESS -> null
            ImportResult.INVALID_RC -> FX.messages["importErrorInvalidRc"]
            ImportResult.INVALID_CONTENT -> FX.messages["importErrorInvalidContent"]
            ImportResult.UNSUPPORTED_CONTENT -> FX.messages["importErrorUnsupportedContent"]
            ImportResult.IMPORT_ERROR -> FX.messages["importErrorImportError"]
            ImportResult.LOAD_RC_ERROR -> FX.messages["importErrorLoadRcError"]
            ImportResult.ALREADY_EXISTS -> FX.messages["importErrorAlreadyExists"]
            ImportResult.UNMATCHED_HELP -> FX.messages["importErrorUnmatchedHelp"]
        }
    }

/** Null on success, otherwise localized error text. */
val ExportResult.errorMessage: String?
    get() {
        return when (this) {
            ExportResult.SUCCESS -> null
            ExportResult.FAILURE -> FX.messages["exportError"]
        }
    }
