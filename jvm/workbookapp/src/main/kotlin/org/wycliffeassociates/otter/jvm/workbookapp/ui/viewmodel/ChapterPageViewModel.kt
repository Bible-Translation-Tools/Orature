/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import com.github.thomasnield.rxkotlinfx.changes
import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Completable
import javafx.beans.binding.Bindings
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentLabel
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.device.IAudioPlayer
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.OtterApp
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
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

    val chapterPlayerProperty = SimpleObjectProperty<IAudioPlayer>()
    val canCompileProperty = SimpleBooleanProperty()
    val selectedChapterTakeProperty = SimpleObjectProperty<Take>()
    val workChunkProperty = SimpleObjectProperty<CardData>()
    val noTakesProperty = SimpleBooleanProperty()

    val chapterCardProperty = SimpleObjectProperty<CardData>(CardData(workbookDataStore.chapter))

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
                checkCanCompile()
                setWorkChunk()
            }

        workbookDataStore.activeChapterProperty.onChangeAndDoNow { _chapter ->
            _chapter?.let { chapter ->
                loadChapterContents(chapter).subscribe()
                val chap = CardData(chapter)
                chapterCardProperty.set(chap)
                setSelectedChapterTake(chap)
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

    fun openPlayers() {
        selectedChapterTakeProperty.value?.let {
            val player = (app as OtterApp).dependencyGraph.injectPlayer()
            player.load(it.file)
            chapterPlayerProperty.set(player)
        }
    }

    fun closePlayers() {
        chapterPlayerProperty.value?.close()
        chapterPlayerProperty.set(null)
    }

    fun checkCanCompile() {
        val hasUnselected = filteredContent.filter { chunk ->
            chunk.chunkSource?.audio?.selected?.value?.value == null
        }.any()
        canCompileProperty.set(hasUnselected.not())
    }

    fun setWorkChunk() {
        if (filteredContent.isEmpty()) return

        val hasTakes = filteredContent.filter { chunk ->
            chunk.chunkSource?.audio?.getAllTakes()?.isNotEmpty() ?: false
        }.any()

        if (hasTakes) {
            val notSelected = filteredContent.filter { chunk ->
                chunk.chunkSource?.audio?.selected?.value?.value == null
            }.firstOrNull() ?: filteredContent.last()
            noTakesProperty.set(false)
            workChunkProperty.set(notSelected)
        } else {
            noTakesProperty.set(true)
            workChunkProperty.set(filteredContent.first())
        }
    }

    private fun setSelectedChapterTake(chapter: CardData) {
        val selected = chapter.chapterSource?.audio?.selected?.value?.value
        val take = chapter.chapterSource?.audio?.getAllTakes()?.singleOrNull {
            it == selected
        }
        selectedChapterTakeProperty.set(take)
    }
}
