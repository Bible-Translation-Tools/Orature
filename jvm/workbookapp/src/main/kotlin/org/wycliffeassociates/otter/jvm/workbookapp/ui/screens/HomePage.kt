/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import javafx.geometry.Pos
import javafx.scene.control.ScrollPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.ImageRatio
import org.wycliffeassociates.otter.jvm.controls.banner.ResumeBookBanner
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import org.wycliffeassociates.otter.jvm.controls.card.NewTranslationCard
import org.wycliffeassociates.otter.jvm.controls.card.TranslationCard
import org.wycliffeassociates.otter.jvm.controls.styles.tryImportStylesheet
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.HomePageViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class HomePage : View() {

    private val viewModel: HomePageViewModel by inject()
    private val settingsViewModel: SettingsViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.set(messages["projects"])
        iconProperty.set(FontIcon(MaterialDesign.MDI_HOME))
        onClickAction {
            navigator.dock(this@HomePage)
        }
    }

    init {
        tryImportStylesheet(resources.get("/css/control.css"))
        tryImportStylesheet(resources.get("/css/home-page.css"))
        tryImportStylesheet(resources.get("/css/resume-book-banner.css"))
        tryImportStylesheet(resources.get("/css/new-translation-card.css"))
        tryImportStylesheet(resources.get("/css/translation-card.css"))
        tryImportStylesheet(resources.get("/css/book-card.css"))
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT

        scrollpane {
            isFitToWidth = true
            hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
            vbox {
                addClass("home-page__container")
                minHeightProperty().bind(this@stackpane.heightProperty())

                add(
                    ResumeBookBanner().apply {
                        resumeTextProperty.set(messages["resume"])
                        orientationScaleProperty.bind(settingsViewModel.orientationScaleProperty)

                        viewModel.resumeBookProperty.onChange {
                            it?.let { workbook ->
                                bookTitleProperty.set(workbook.target.title)
                                backgroundArtworkProperty.set(
                                    workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)
                                )
                                sourceLanguageProperty.set(workbook.source.language.name)
                                targetLanguageProperty.set(workbook.target.language.name)
                                onResumeAction {
                                    viewModel.selectProject(workbook)
                                }
                            }
                        }

                        visibleProperty().bind(viewModel.resumeBookProperty.isNotNull)
                        managedProperty().bind(visibleProperty())
                    }
                )

                add(
                    NewTranslationCard().apply {
                        newTranslationTextProperty.set(messages["createTranslation"])
                        orientationScaleProperty.bind(settingsViewModel.orientationScaleProperty)
                        setOnAction {
                            viewModel.createTranslation()
                        }
                    }
                )

                vbox {
                    maxWidth = 800.0
                    spacing = 20.0
                    bindChildren(viewModel.translationModels) {
                        TranslationCard(it.sourceLanguage.name, it.targetLanguage.name, it.books).apply {
                            setConverter { workbook ->
                                BookCard().apply {
                                    titleProperty.set(workbook.target.title)
                                    slugProperty.set(workbook.target.slug.uppercase())
                                    coverArtProperty.set(
                                        workbook.artworkAccessor.getArtwork(ImageRatio.TWO_BY_ONE)
                                    )

                                    setOnPrimaryAction { viewModel.selectProject(workbook) }
                                }
                            }

                            showMoreTextProperty.set(messages["showMore"])
                            showLessTextProperty.set(messages["showLess"])
                            orientationScaleProperty.bind(settingsViewModel.orientationScaleProperty)

                            setOnNewBookAction {
                                viewModel.createProject(it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.loadResumeBook()
        viewModel.loadTranslations()
        viewModel.clearSelectedProject()
    }
}
