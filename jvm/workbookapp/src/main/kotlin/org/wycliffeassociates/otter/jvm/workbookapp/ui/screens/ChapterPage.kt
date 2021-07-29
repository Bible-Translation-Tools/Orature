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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.data.primitives.ContentType
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.DefaultStyles
import org.wycliffeassociates.otter.jvm.controls.card.card
import org.wycliffeassociates.otter.jvm.controls.skins.cards.ChapterBanner
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppStyles
import org.wycliffeassociates.otter.jvm.workbookapp.theme.AppTheme
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.styles.CardGridStyles
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*

class ChapterPage : Fragment() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: ChapterPageViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
    private val banner: ChapterBanner
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(viewModel.breadcrumbTitleBinding(this@ChapterPage))
        iconProperty.set(FontIcon(MaterialDesign.MDI_BOOKMARK))
        onClickAction {
            navigator.dock(this@ChapterPage)
        }
    }

    override fun onDock() {
        workbookDataStore.activeChunkProperty.set(null)
        workbookDataStore.activeResourceComponentProperty.set(null)
        workbookDataStore.activeResourceProperty.set(null)
        navigator.dock(this, breadCrumb)
    }

    init {
        banner = ChapterBanner().apply {
            viewModel.chapterCard.onChangeAndDoNow {
                it?.let {
                    bookTitle.text = viewModel.workbookDataStore.workbook.target.title
                    chapterCount.text = viewModel.workbookDataStore.activeChapterProperty.value?.title
                    openButton.text = messages["open"]
                    viewModel
                        .workbookDataStore
                        .activeChapterProperty
                        .value
                        ?.let { chapter ->
                            chapter.chunks
                                .filter {
                                    it.contentType == ContentType.TEXT
                                }
                                .count()
                                .doOnError { e ->
                                    logger.error("Error in setting chapter banner chunk count", e)
                                }
                                .subscribe { count ->
                                    Platform.runLater {
                                        chunkCount.text = count.toString()
                                    }
                                }
                            openButton.setOnMouseClicked {
                                viewModel.onCardSelection(CardData(chapter))
                                navigator.dock<RecordScriptureFragment>()
                            }
                        }
                }
            }
        }
    }

    override val root = vbox {
        add(banner)
        datagrid(viewModel.filteredContent) {
            vgrow = Priority.ALWAYS
            hgrow = Priority.ALWAYS
            isFillWidth = true
            addClass(AppStyles.whiteBackground)
            addClass(CardGridStyles.contentContainer)
            cellCache { item ->
                card {
                    addClass(DefaultStyles.defaultCard)
                    cardfront {
                        innercard(cardGraphic()) {
                            title = item.item.toUpperCase()
                            bodyText = item.bodyText
                        }
                        cardbutton {
                            addClass(DefaultStyles.defaultCardButton)
                            text = messages["openProject"]
                            graphic = FontIcon("gmi-arrow-forward")
                                .apply {
                                    iconSize = 25
                                    iconColor = AppTheme.colors.appRed
                                }
                            onMousePressed = EventHandler {
                                viewModel.onCardSelection(item)
                                navigator.dock<RecordScriptureFragment>()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cardGraphic(): Node {
        if (viewModel.filteredContent.first().dataType == "content") {
            return AppStyles.chunkGraphic()
        }
        return AppStyles.chapterGraphic()
    }
}
