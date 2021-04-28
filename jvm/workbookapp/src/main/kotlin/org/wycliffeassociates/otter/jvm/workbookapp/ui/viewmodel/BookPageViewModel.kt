package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourcePage
import tornadofx.*

class BookPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(BookPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()
    val navigator: NavigationMediator by inject()

    val allContent: ObservableList<CardData> = FXCollections.observableArrayList()
    val currentTabProperty = SimpleStringProperty("ulb")

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(BookPageViewModel::loading)

    var chapterOpen = SimpleBooleanProperty(false)

    init {
        workbookDataStore.activeWorkbookProperty.onChangeAndDoNow {
            it?.let { wb -> loadChapters(wb) }
        }

        workbookDataStore.activeChapterProperty.onChange { chapter ->
            when (chapter) {
                null -> workbookDataStore.activeWorkbookProperty.value?.let { workbook ->
                    chapterOpen.set(false)
                    loadChapters(workbook)
                }
                else -> {
                    chapterOpen.value = true
                }
            }
        }
    }

    private fun loadChapters(workbook: Workbook) {
        loading = true
        allContent.clear()
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
                allContent.setAll(list)
            }
    }

    fun navigate(resourceMetadata: ResourceMetadata) {
        when (resourceMetadata.type) {
            ContainerType.Book, ContainerType.Bundle -> navigator.dock<ChapterPage>()
            ContainerType.Help -> navigator.dock<ResourcePage>()
        }
    }
}
