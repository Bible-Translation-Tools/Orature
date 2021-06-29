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
