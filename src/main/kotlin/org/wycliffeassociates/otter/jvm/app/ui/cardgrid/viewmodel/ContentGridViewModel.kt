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
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardData
import org.wycliffeassociates.otter.jvm.app.ui.cardgrid.CardDataMapper
import org.wycliffeassociates.otter.jvm.app.ui.inject.Injector
import tornadofx.*

class ContentGridViewModel: ViewModel() {

    private val injector: Injector by inject()
    private val collectionRepository = injector.collectionRepo
    private val contentRepository = injector.contentRepository

    // The selected project
    private var activeProject: Collection by property()
    val activeProjectProperty = getProperty(ContentGridViewModel::activeProject)

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

        activeProjectProperty.toObservable().subscribe {
            bindChapters()
        }
        activeCollectionProperty.onChange {
            if(it != null) {
                selectChildCollection(it)
            }
            else bindChapters()
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
        if(cardData.collectionSource != null) {
            activeCollection = cardData.collectionSource
        }

        else if (cardData.contentSource != null) {
            activeContent = cardData.contentSource
        }
    }

    private fun bindChapters() {
        activeCollectionProperty.value = null
        loading = true
        if (activeProject != null) {
            allContent.clear()
            collectionRepository
                .getChildren(activeProject)
                .observeOnFx()
                .subscribe { childCollections ->
                    // Now we have the children of the project collection
                    loading = false
                    val cardList = CardDataMapper.mapCollectionListToCards(childCollections)
                    allContent.addAll(cardList.sortedBy { it.sort })
                }
        }
    }
}