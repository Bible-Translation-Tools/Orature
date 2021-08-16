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

import javafx.scene.layout.Priority
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.components.ChunkCell
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ChapterPageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookDataStore
import tornadofx.*
import java.text.MessageFormat

class ChapterPage : Fragment() {
    private val logger = LoggerFactory.getLogger(ChapterPage::class.java)

    private val viewModel: ChapterPageViewModel by inject()
    private val workbookDataStore: WorkbookDataStore by inject()
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
        importStylesheet(resources.get("/css/chapter-page.css"))
    }

    override val root = hbox {
        addClass("chapter-page")
        vbox {
            addClass("chapter-page__chapter-info")
            vgrow = Priority.ALWAYS

            vbox {
                addClass("chapter-page__chapter-box")
                vgrow = Priority.ALWAYS

                viewModel.chapterCard.onChangeAndDoNow {
                    label {
                        addClass("chapter-page__chapter-title")
                        text = MessageFormat.format(
                            FX.messages["chapterTitle"],
                            FX.messages["chapter"].capitalize(),
                            viewModel.workbookDataStore.activeChapterProperty.value?.title
                        )
                    }

                    hbox {
                        addClass("chapter-page__chapter-audio")
                        vgrow = Priority.ALWAYS

                        label("Drafting not Started")
                    }

                    hbox {
                        addClass("chapter-page__chapter-actions")
                        button {
                            addClass("btn", "btn--secondary")
                            text = "Record Chapter"
                            graphic = FontIcon(MaterialDesign.MDI_MICROPHONE)
                            viewModel.workbookDataStore.activeChapterProperty.value?.let { chapter ->
                                setOnAction {
                                    viewModel.onCardSelection(CardData(chapter))
                                    navigator.dock<RecordScriptureFragment>()
                                }
                            }
                        }
                        button {
                            addClass("btn", "btn--secondary")
                            text = "Continue Translation"
                            graphic = FontIcon(MaterialDesign.MDI_VOICE)
                        }
                    }
                }
            }

            vbox {
                addClass("chapter-page__actions")
                vgrow = Priority.ALWAYS


                button {
                    addClass("btn", "btn--primary")
                    text = "Edit Chapter"
                    graphic = FontIcon(MaterialDesign.MDI_PENCIL)
                }
                button {
                    addClass("btn", "btn--primary")
                    text = "Add Verse Markers"
                    graphic = FontIcon(MaterialDesign.MDI_BOOKMARK)
                }
                button {
                    addClass("btn", "btn--primary")
                    text = "View Takes"
                    graphic = FontIcon(MaterialDesign.MDI_LIBRARY_MUSIC)
                }
            }
        }

        vbox {
            addClass("chapter-page__chunks")
            vgrow = Priority.ALWAYS

            button {
                addClass("btn", "btn--primary")
                text = "Compile"
                graphic = FontIcon(MaterialDesign.MDI_LAYERS)
            }

            listview(viewModel.filteredContent) {
                fitToParentHeight()
                setCellFactory {
                    ChunkCell {
                        viewModel.onCardSelection(it)
                        navigator.dock<RecordScriptureFragment>()
                    }
                }
            }
        }
    }
}
