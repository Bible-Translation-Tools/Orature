package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourcePage
import tornadofx.*

class WorkbookPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(WorkbookPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()

    val chapters: ObservableList<CardData> = FXCollections.observableArrayList()
    val currentTabProperty = SimpleStringProperty()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(WorkbookPageViewModel::loading)

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
            .map { CardData(it) }
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnError { e ->
                logger.error("Error in loading chapters for project: ${workbook.target.slug}", e)
            }
            .subscribe { list: List<CardData> ->
                chapters.setAll(list)
            }
    }

    /**
     * Opens the next page of the workbook based on the selected chapter.
     * This updates the active properties of the WorkbookDataStore and selects
     * the appropriate page based on which resource the User was in.
     */
    fun navigate(chapter: Chapter) {
        workbookDataStore.activeChapterProperty.set(chapter)
        val resourceMetadata = workbookDataStore.activeResourceMetadata
        when (resourceMetadata.type) {
            ContainerType.Book, ContainerType.Bundle -> workspace.dock<ChapterPage>()
            ContainerType.Help -> workspace.dock<ResourcePage>()
        }
    }
}
