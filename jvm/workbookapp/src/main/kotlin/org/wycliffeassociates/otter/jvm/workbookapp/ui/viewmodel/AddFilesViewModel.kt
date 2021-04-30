package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.errorMessage
import tornadofx.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class AddFilesViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(AddFilesViewModel::class.java)

    @Inject
    lateinit var importRcProvider: Provider<ImportResourceContainer>

    val showImportDialogProperty = SimpleBooleanProperty(false)

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDropFile(files: List<File>) {
        if (isValidImportFile(files)) {
            importResourceContainer(files.first())
        }
    }

    fun onChooseFile() {
        val file = chooseFile(
            FX.messages["importResourceFromZip"],
            arrayOf(FileChooser.ExtensionFilter(messages["oratureFileTypes"], "*.zip")),
            mode = FileChooserMode.Single
        ).firstOrNull()
        file?.let {
            importResourceContainer(file)
        }
    }

    private fun importResourceContainer(file: File) {
        showImportDialogProperty.set(true)

        importRcProvider.get()
            .import(file)
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in importing resource container $file", e)
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

    private fun isValidImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                false
            }
            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                false
            }
            files.first().extension != "zip" -> {
                snackBarObservable.onNext(messages["importInvalidFileError"])
                false
            }
            else -> true
        }
    }
}
