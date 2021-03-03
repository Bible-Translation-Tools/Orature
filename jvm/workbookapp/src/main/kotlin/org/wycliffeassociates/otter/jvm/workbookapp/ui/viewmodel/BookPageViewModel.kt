package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.observeOnFx
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ResourceListFragment
import tornadofx.*

class BookPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()

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

    fun onCardSelection(cardData: CardData) {
        cardData.chapterSource?.let {
            workbookDataStore.activeChapterProperty.set(it)
        }
        // Chunk will be null if the chapter recording is opened. This needs to happen to update the recordable to
        // use the chapter recordable.
        workbookDataStore.activeChunkProperty.set(cardData.chunkSource)
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
                // TODO
                // setAll is causing the UI to hang, probably because node structure is complex. If "loading" is
                // set to false after this operation, the spinner will remain but stop spinning while the UI hangs.
                allContent.setAll(list)
            }
    }

    fun navigate(chapter: Chapter) {
        val currentTab = currentTabProperty.value
        when (currentTab.toLowerCase()) {
            "ulb" -> workspace.dock<ChapterPage>()
            else -> workspace.dock<ResourceListFragment>()
        }
    }
}
