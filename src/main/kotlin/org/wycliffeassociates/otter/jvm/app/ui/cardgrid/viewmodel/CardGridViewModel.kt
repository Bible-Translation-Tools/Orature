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
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class CardGridViewModel: ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    private val allContent: ObservableList<CardData>
            = FXCollections.observableArrayList()
    val filteredContent: ObservableList<CardData>
            = FXCollections.observableArrayList()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(CardGridViewModel::loading)

    private val chapterModeEnabledProperty = SimpleBooleanProperty(false)

    init {
        Observable.merge(
            chapterModeEnabledProperty.toObservable(),
            allContent.changes()
        ).subscribe {
            filteredContent.setAll(
                if (chapterModeEnabledProperty.value == true) {
                    allContent.filtered { cardData ->
                        cardData.item == ContentLabel.CHAPTER.value
                    }
                } else {
                    allContent
                }
            )
        }

        workbookViewModel.activeWorkbookProperty.onChangeAndDoNow {
            it?.let { wb -> loadChapters(wb) }
        }

        workbookViewModel.activeChapterProperty.onChange { chapter ->
            when (chapter) {
                null -> workbookViewModel.activeWorkbookProperty.value?.let {
                    workbook -> loadChapters(workbook)
                }
                else -> loadChapterContents(chapter)
            }
        }
    }

    private fun loadChapterContents(chapter: Chapter) {
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        chapter.chunks
            .map{CardData(it)}
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .subscribe { list: List<CardData> ->
                allContent.setAll(list)
            }
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
            .map{CardData(it)}
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .subscribe { list: List<CardData> ->
                // TODO
                // setAll is causing the UI to hang, probably because node structure is complex. If "loading" is
                // set to false after this operation, the spinner will remain but stop spinning while the UI hangs.
                allContent.setAll(list)
            }
        }
}