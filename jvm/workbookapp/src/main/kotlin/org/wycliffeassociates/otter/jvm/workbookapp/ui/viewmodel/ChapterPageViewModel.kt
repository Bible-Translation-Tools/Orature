package org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.ChapterPage
import tornadofx.*
import java.text.MessageFormat
import java.util.concurrent.Callable

class ChapterPageViewModel : ViewModel() {

    private val logger = LoggerFactory.getLogger(ChapterPageViewModel::class.java)

    val workbookDataStore: WorkbookDataStore by inject()

    // List of content to display on the screen
    // Boolean tracks whether the content has takes associated with it
    private val allContent: ObservableList<CardData> = FXCollections.observableArrayList()
    val filteredContent: ObservableList<CardData> = FXCollections.observableArrayList()

    private var loading: Boolean by property(false)
    val loadingProperty = getProperty(ChapterPageViewModel::loading)

    val chapterCard = SimpleObjectProperty<CardData>(CardData(workbookDataStore.chapter))

    private val navigator: NavigationMediator by inject()

    init {
        allContent
            .changes()
            .doOnError { e ->
                logger.error("Error in setting up content cards", e)
            }
            .subscribe {
                filteredContent.setAll(
                    allContent.filtered { cardData ->
                        cardData.item != ContentLabel.CHAPTER.value
                    }
                )
            }

        workbookDataStore.activeChapterProperty.onChangeAndDoNow { _chapter ->
            _chapter?.let { chapter ->
                loadChapterContents(chapter).subscribe()
                val chap = CardData(chapter)
                chapterCard.set(chap)
            }
        }
    }

    fun breadcrumbTitleBinding(view: UIComponent): StringBinding {
        return Bindings.createStringBinding(
            Callable {
                when {
                    workbookDataStore.activeChunkProperty.value != null ->
                        workbookDataStore.activeChunkProperty.value.let { chunk ->
                            MessageFormat.format(
                                messages["chunkTitle"],
                                messages[ContentLabel.of(chunk.contentType).value],
                                chunk.start
                            )
                        }
                    navigator.workspace.dockedComponentProperty.value == view -> messages["chunk"]
                    else -> messages["chapter"]
                }
            },
            navigator.workspace.dockedComponentProperty,
            workbookDataStore.activeChunkProperty
        )
    }

    private fun loadChapterContents(chapter: Chapter): Completable {
        // Remove existing content so the user knows they are outdated
        allContent.clear()
        loading = true
        return chapter.chunks
            .map { CardData(it) }
            .doOnComplete {
                loading = false
            }
            .observeOnFx()
            .toList()
            .doOnError { e ->
                logger.error("Error in loading chapter contents for chapter: $chapter", e)
            }
            .map { list: List<CardData> ->
                allContent.setAll(list)
            }.ignoreElement()
    }

    fun onCardSelection(cardData: CardData) {
        cardData.chapterSource?.let {
            workbookDataStore.activeChapterProperty.set(it)
        }
        // Chunk will be null if the chapter recording is opened. This needs to happen to update the recordable to
        // use the chapter recordable.
        workbookDataStore.activeChunkProperty.set(cardData.chunkSource)
    }
}
