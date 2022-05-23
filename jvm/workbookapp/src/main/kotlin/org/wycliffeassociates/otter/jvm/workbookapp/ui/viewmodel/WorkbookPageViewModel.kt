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
import io.reactivex.Completable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.common.domain.collections.DeleteProject
import org.wycliffeassociates.otter.common.domain.resourcecontainer.project.ProjectFilesAccessor
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ExportResult
import org.wycliffeassociates.otter.common.domain.resourcecontainer.projectimportexport.ProjectExporter
import org.wycliffeassociates.otter.common.persistence.repositories.IAppPreferencesRepository
import org.wycliffeassociates.otter.common.persistence.repositories.IWorkbookRepository
import org.wycliffeassociates.otter.jvm.workbookapp.di.IDependencyGraphProvider
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ContributorCellData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourcePage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.system.errorMessage
import tornadofx.*
import java.io.File
import java.text.MessageFormat
import javax.inject.Inject
import javax.inject.Provider

class WorkbookPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(WorkbookPageViewModel::class.java)

    @Inject
    lateinit var deleteProjectProvider: Provider<DeleteProject>

    @Inject
    lateinit var projectExporterProvider: Provider<ProjectExporter>

    @Inject
    lateinit var workbookRepository: IWorkbookRepository

    @Inject
    lateinit var preferencesRepository: IAppPreferencesRepository

    val workbookDataStore: WorkbookDataStore by inject()

    val chapters: ObservableList<ChapterCardModel> = FXCollections.observableArrayList()
    val contributors = observableListOf<Contributor>()
    val currentTabProperty = SimpleStringProperty()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(WorkbookPageViewModel::loading)

    val showDeleteProgressDialogProperty = SimpleBooleanProperty(false)
    val showExportProgressDialogProperty = SimpleBooleanProperty(false)

    val activeProjectTitleProperty = SimpleStringProperty()
    val activeProjectCoverProperty = SimpleObjectProperty<File>()

    val selectedChapterProperty = SimpleObjectProperty<Chapter>()
    val showDeleteDialogProperty = SimpleBooleanProperty(false)
    val showDeleteSuccessDialogProperty = SimpleBooleanProperty(false)
    val showDeleteFailDialogProperty = SimpleBooleanProperty(false)
    val selectedResourceMetadata = SimpleObjectProperty<ResourceMetadata>()

    private val navigator: NavigationMediator by inject()
    private var projectFilesAccessorListener: ChangeListener<ProjectFilesAccessor>? = null

    init {
        (app as IDependencyGraphProvider).dependencyGraph.inject(this)
        
        projectFilesAccessorListener = ChangeListener<ProjectFilesAccessor> { _, _, projectAccessor ->
            if (projectAccessor != null) {
                val projectContributors = projectAccessor.getContributorInfo()
                contributors.setAll(projectContributors)
            }
        }
    }

    fun dock() {
        workbookDataStore.activeProjectFilesAccessorProperty.addListener(
            projectFilesAccessorListener
        )
    }

    /**
     * Initializes the workbook for use and loads UI representations of the chapters in the workbook.
     *
     * As this could happen from the user returning from deeper within the workbook,
     * we null out the active chapter, as we have returned from being in a chapter.
     */
    fun openWorkbook() {
        workbookDataStore.activeChapterProperty.set(null)
        loadChapters(workbookDataStore.workbook)
    }

    /**
     * Sets WorkbookDataStore state to represent the selected workbook tab.
     */
    fun openTab(resourceMetadata: ResourceMetadata) {
        currentTabProperty.set(resourceMetadata.identifier)
        workbookDataStore.activeResourceMetadataProperty.set(resourceMetadata)
        workbookDataStore.setProjectFilesAccessor(resourceMetadata)
    }

    /**
     * Retrieves the metadata of the book and all related resources making up the Workbook
     */
    fun getAllBookResources(): List<ResourceMetadata> {
        val currentTarget = workbookDataStore.workbook.target
        return listOf(
            currentTarget.resourceMetadata,
            *currentTarget.linkedResources.toTypedArray()
        )
    }

    /**
     * Retrieves chapters out of the workbook and maps them to UI representations
     */
    private fun loadChapters(workbook: Workbook) {
        loading = true
        chapters.clear()
        workbook.target.chapters
            .map { chapter ->
                ChapterCardModel(
                    title = MessageFormat.format(
                        FX.messages["chapterTitle"],
                        FX.messages["chapter"],
                        chapter.sort
                    ),
                    chapter = chapter,
                    onClick = { navigate(chapter) }
                )
            }
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnError { e ->
                logger.error("Error in loading chapters for project: ${workbook.target.slug}", e)
            }
            .subscribe { list: List<ChapterCardModel> ->
                chapters.addAll(list)
            }
    }

    fun getLastResource(): String {
        return preferencesRepository.lastResource().blockingGet()
    }

    private fun updateLastResource(resource: String) {
        preferencesRepository.setLastResource(resource).subscribe()
    }

    /**
     * Opens the next page of the workbook based on the selected chapter.
     * This updates the active properties of the WorkbookDataStore and selects
     * the appropriate page based on which resource the User was in.
     */
    fun navigate(chapter: Chapter) {
        selectedChapterProperty.set(chapter)
        workbookDataStore.activeChapterProperty.set(chapter)
        val resourceMetadata = workbookDataStore.activeResourceMetadata
        updateLastResource(resourceMetadata.identifier)
        when (resourceMetadata.type) {
            ContainerType.Book, ContainerType.Bundle -> navigator.dock<ChapterPage>()
            ContainerType.Help -> navigator.dock<ResourcePage>()
        }
    }

    fun exportWorkbook(directory: File) {
        showExportProgressDialogProperty.set(true)

        val workbook = workbookDataStore.workbook
        val projectExporter = projectExporterProvider.get()
        val resourceMetadata = workbookDataStore.activeResourceMetadata
        val projectFileAccessor = workbookDataStore.activeProjectFilesAccessor

        activeProjectTitleProperty.set(workbook.target.title)
        activeProjectCoverProperty.set(
            workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)?.file
        )

        projectExporter
//            .export(directory, resourceMetadata, workbook, projectFileAccessor)
            .exportAsSource(directory, resourceMetadata, workbook, projectFileAccessor)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in exporting project for project: ${workbook.target.slug}")
                logger.error("Project language: ${workbook.target.language.slug}, file: $directory", e)
            }
            .doFinally {
                activeProjectTitleProperty.set(null)
                activeProjectCoverProperty.set(null)
            }
            .subscribe { result: ExportResult ->
                showExportProgressDialogProperty.set(false)

                result.errorMessage?.let {
                    error(messages["exportError"], it)
                }
            }
    }

    fun deleteWorkbook() {
        showDeleteDialogProperty.set(false)
        showDeleteProgressDialogProperty.set(true)
        val workbook = workbookDataStore.workbook
        val deleteProject = deleteProjectProvider.get()

        activeProjectTitleProperty.set(workbook.target.title)
        activeProjectCoverProperty.set(
            workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)?.file
        )

        workbookRepository.closeWorkbook(workbook)
        deleteProject
            .delete(workbook, true)
            .observeOnFx()
            .doOnError { e ->
                logger.error("Error in deleting project: ${workbook.target.slug} ${workbook.target.language.slug}", e)
            }
            .doFinally {
                activeProjectTitleProperty.set(null)
                activeProjectCoverProperty.set(null)
                showDeleteProgressDialogProperty.set(false)
            }
            .subscribe(
                {
                    showDeleteSuccessDialogProperty.set(true)
                    preferencesRepository.setResumeProjectId(NO_RESUMABLE_PROJECT).subscribe()
                },
                {
                    showDeleteFailDialogProperty.set(true)
                }
            )
    }

    fun addContributor(name: String) {
        contributors.add(0, Contributor(name))
    }

    fun editContributor(data: ContributorCellData) {
        // replace the object in list to trigger onChange
        contributors[data.index] = Contributor(data.name)
    }

    fun removeContributor(index: Int) {
        contributors.removeAt(index)
    }

    fun saveContributorInfo() {
        Completable
            .fromAction {
                workbookDataStore.activeProjectFilesAccessor.setContributorInfo(contributors)
            }
            .observeOnFx()
            .doOnError {
                logger.error("Error saving contributor to project rc.", it)
            }
            .subscribe()
    }

    fun undock() {
        workbookDataStore.activeProjectFilesAccessorProperty.removeListener(
            projectFilesAccessorListener
        )
    }

    fun goBack() {
        showDeleteSuccessDialogProperty.set(false)
        navigator.back()
    }
}
