package org.wycliffeassociates.otter.jvm.app.ui.cardgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardData
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardDataMapper
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ContentGridViewModel: ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    val allContent: ObservableList<CardData>
            = FXCollections.observableArrayList()
    val filteredContent: ObservableList<CardData>
            = FXCollections.observableArrayList()

    // Whether the UI should show the plugin as active
    private var showPluginActive: Boolean by property(false)
    val showPluginActiveProperty = getProperty(ContentGridViewModel::showPluginActive)

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ContentGridViewModel::loading)

    val chapterModeEnabledProperty = SimpleBooleanProperty(false)

    init {
        Observable.merge(chapterModeEnabledProperty.toObservable(), allContent.changes()).subscribe { _ ->
            filteredContent.setAll(
                    if (chapterModeEnabledProperty.value == true) {
                        allContent.filtered { it.item == ContentLabel.CHAPTER.value }
                    } else {
//                        allContent.filtered { it.item != ContentLabelEnum.CHAPTER.value }
                        allContent
                    }
            )
        }

        workbookViewModel.activeWorkbookProperty.onChangeAndDoNow {
            it?.let { wb -> loadChapters(wb) }
        }

        workbookViewModel.activeChapterProperty.onChange {
            it?.let { ch -> loadChapterContents(ch) }
                ?: workbookViewModel.activeWorkbookProperty.value
                    ?.let { wb -> loadChapters(wb) }
        }
    }

    private fun loadChapterContents(chapter: Chapter) {
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        chapter.chunks
            .map(CardDataMapper.Companion::mapChunkToCardData)
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnSuccess {
                allContent.setAll(it)
            }.subscribe()
    }

    fun onCardSelection(cardData: CardData){
        cardData.chapterSource?.let {
            workbookViewModel.activeChapterProperty.set(it)
        }
        cardData.chunkSource?.let {
            workbookViewModel.activeChunkProperty.set(it)
        }
    }

    private fun loadChapters(workbook: Workbook) {
        loading = true
        allContent.clear()
        workbook.target.chapters
            .map(CardDataMapper.Companion::mapChapterToCardData)
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnSuccess {
                // TODO
                // setAll is causing the UI to hang, probably because node structure is complex. If "loading" is
                // set to false after this operation, the spinner will remain but stop spinning while the UI hangs.
                allContent.setAll(it)
            }.subscribe()
        }
}