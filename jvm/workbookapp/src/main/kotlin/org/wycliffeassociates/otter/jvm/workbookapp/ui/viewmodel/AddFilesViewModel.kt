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
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.OratureFileFormat
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.domain.resourcecontainer.artwork.ArtworkAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResourceContainer
import org.wycliffeassociates.otter.common.domain.resourcecontainer.ImportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectImporter
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
    @Inject lateinit var importRcProvider: Provider<ImportResourceContainer>
    @Inject lateinit var importProvider: Provider<ProjectImporter>

    val showImportDialogProperty = SimpleBooleanProperty(false)
    val showImportSuccessDialogProperty = SimpleBooleanProperty(false)
    val showImportErrorDialogProperty = SimpleBooleanProperty(false)
    val importedProjectTitleProperty = SimpleStringProperty()
    val importedProjectCoverProperty = SimpleObjectProperty<File>()

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
            .doFinally {
                importedProjectTitleProperty.set(null)
                importedProjectCoverProperty.set(null)
            }
            .subscribe { result: ImportResult ->
                if (result == ImportResult.SUCCESS) {
                    showImportSuccessDialogProperty.value = true
                    find<HomePageViewModel>().loadTranslations()
                } else {
                    showImportErrorDialogProperty.value = true
                }

                showImportDialogProperty.value = false
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
            files.first().extension !in OratureFileFormat.extensionList -> {
                snackBarObservable.onNext(messages["importInvalidFileError"])
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
