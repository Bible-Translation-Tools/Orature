/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.domain.project.ImportProjectUseCase
import org.wycliffeassociates.otter.common.domain.project.importer.ImportCallbackParameter
import org.wycliffeassociates.otter.common.domain.project.importer.ImportOptions
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.project.importer.OngoingProjectImporter
import org.wycliffeassociates.otter.common.domain.project.importer.ProjectImporterCallback
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.resourcecontainer.ResourceContainer
import tornadofx.*
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class AddFilesViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(AddFilesViewModel::class.java)

    @Inject lateinit var directoryProvider: IDirectoryProvider
    @Inject lateinit var importProjectProvider : Provider<ImportProjectUseCase>
    @Inject lateinit var importProvider: Provider<OngoingProjectImporter>

    val showImportDialogProperty = SimpleBooleanProperty(false)
    val showImportSuccessDialogProperty = SimpleBooleanProperty(false)
    val showImportErrorDialogProperty = SimpleBooleanProperty(false)
    val importErrorMessage = SimpleStringProperty(null)
    val importedProjectTitleProperty = SimpleStringProperty()
    val importedProjectCoverProperty = SimpleObjectProperty<File>()

    val snackBarObservable: PublishSubject<String> = PublishSubject.create()

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
    }

    fun onDropFile(files: List<File>) {
        if (isValidImportFile(files)) {
            logger.info("Drag-drop file to import: ${files.first()}")
            importProject(files.first())
        }
    }

    fun onChooseFile() {
        val file = chooseFile(
            FX.messages["importResourceFromZip"],
            arrayOf(
                FileChooser.ExtensionFilter(
                    messages["oratureFileTypes"],
                    *OratureFileFormat.extensionList.map { "*.$it" }.toTypedArray()
                )
            ),
            mode = FileChooserMode.Single
        ).firstOrNull()
        file?.let {
            setProjectInfo(file)
            importProject(file)
        }
    }

    private fun importProject(file: File) {
        showImportDialogProperty.set(true)
        val callback = setupImportCallback()

        importProjectProvider.get()
            .import(file, callback)
            .subscribeOn(Schedulers.io())
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in importing resource container $file", e)
            }
            .doFinally {
                importedProjectTitleProperty.set(null)
                importedProjectCoverProperty.set(null)
            }
            .subscribe { result: ImportResult ->
                when (result) {
                    ImportResult.SUCCESS -> {
                        showImportSuccessDialogProperty.value = true
                        find<HomePageViewModel>().loadTranslations()
                    }
                    ImportResult.FAILED -> {
                        importErrorMessage.set(messages["importErrorDependencyExists"])
                        showImportErrorDialogProperty.value = true
                    }
                    else -> {
                        showImportErrorDialogProperty.value = true
                    }
                }
                showImportDialogProperty.value = false
            }
    }

    private fun setupImportCallback(): ProjectImporterCallback {
        return object : ProjectImporterCallback {
            override fun onRequestUserInput(): Single<ImportOptions> {
                TODO("Not yet implemented")
            }

            override fun onRequestUserInput(parameter: ImportCallbackParameter): Single<ImportOptions> {
                println("select options: " + parameter.options)
                return Single.just(ImportOptions(listOf(5)))
                TODO("Not yet implemented")
            }

            override fun onError() {
                TODO("Not yet implemented")
            }

        }
    }

    private fun isValidImportFile(files: List<File>): Boolean {
        return when {
            files.size > 1 -> {
                snackBarObservable.onNext(messages["importMultipleError"])
                logger.error(
                    "(Drag-Drop) Multi-files import is not supported. Input files: $files"
                )
                false
            }
            files.first().isDirectory -> {
                snackBarObservable.onNext(messages["importDirectoryError"])
                logger.error(
                    "(Drag-Drop) Directory import is not supported. Input path: ${files.first()}"
                )
                false
            }
            files.first().extension !in OratureFileFormat.extensionList -> {
                snackBarObservable.onNext(messages["importInvalidFileError"])
                logger.error(
                    "(Drag-Drop) Invalid import file extension. Input files: ${files.first()}"
                )
                false
            }
            else -> true
        }
    }

    private fun setProjectInfo(rc: File) {
        try {
            val project = ResourceContainer.load(rc, true).use { it.project() }
            project?.let {
                importProvider.get()
                    .getSourceMetadata(rc)
                    .doOnError {
                        logger.debug("Error in getSourceMetadata: $rc")
                    }
                    .onErrorComplete()
                    .subscribe { resourceMetadata ->
                        resourceMetadata?.let {
                            importedProjectTitleProperty.set(project.title)
                            val coverArtAccessor = ArtworkAccessor(directoryProvider, it, project.identifier)
                            importedProjectCoverProperty.set(
                                coverArtAccessor.getArtwork(ImageRatio.FOUR_BY_ONE)?.file
                            )
                        }
                    }
            }
        } catch (e: Exception) {
            logger.error("Error in getting info from resource container $rc", e)
        }
    }
}
