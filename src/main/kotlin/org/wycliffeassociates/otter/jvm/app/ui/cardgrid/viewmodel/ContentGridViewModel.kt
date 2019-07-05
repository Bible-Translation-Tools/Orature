package org.wycliffeassociates.otter.jvm.app.ui.cardgrid.viewmodel

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import com.github.thomasnield.rxkotlinfx.toObservable
import io.reactivex.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.data.model.Collection
import org.wycliffeassociates.otter.common.data.model.Content
import org.wycliffeassociates.otter.common.data.model.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Workbook
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardData
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardDataMapper
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import org.wycliffeassociates.otter.jvm.app.ui.workbook.viewmodel.WorkbookViewModel
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ContentGridViewModel: ViewModel() {
    private val workbookViewModel: WorkbookViewModel by inject()

    private val injector: Injector by inject()
    private val collectionRepository = injector.collectionRepo
    private val contentRepository = injector.contentRepository

    // Selected child
    private var activeCollection: Collection by property()
    val activeCollectionProperty = getProperty(ContentGridViewModel::activeCollection)

    // Selected content (chunk or verse)
    private var activeContent: Content by property()
    val activeContentProperty = getProperty(ContentGridViewModel::activeContent)

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
        activeCollectionProperty.toObservable().subscribe { selectChildCollection(it) }
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
            it?.let { wb -> bindChapters(wb) }
        }
        activeCollectionProperty.onChange {
            if(it != null) {
                selectChildCollection(it)
            }
            else {
                // TODO
                workbookViewModel.activeWorkbookProperty.value?.let {
                    bindChapters(it)
                }
            }
        }
    }

    private fun selectChildCollection(child: Collection) {
        activeCollection = child
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        contentRepository
                .getByCollection(child)
                .observeOnFx()
                .subscribe { retrieved ->
                    retrieved.sortedBy { it.sort }
                    allContent.clear() // Make sure any content that might have been added are removed
                    val cardList = CardDataMapper.mapContentListToCards(retrieved)
                    allContent.setAll(cardList)
                    loading = false
                }
    }

    fun onCardSelection(cardData: CardData){
        // TODO
//        if(cardData.collectionSource != null) {
//            activeCollection = cardData.collectionSource
//        }

        if (cardData.contentSource != null) {
            activeContent = cardData.contentSource
        }
    }

    private fun bindChapters(workbook: Workbook) {
        activeCollectionProperty.value = null
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