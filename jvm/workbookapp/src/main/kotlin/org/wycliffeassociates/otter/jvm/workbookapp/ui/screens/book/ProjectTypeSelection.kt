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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.book

import javafx.geometry.Pos
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.jvm.controls.breadcrumbs.BreadCrumb
import org.wycliffeassociates.otter.jvm.workbookapp.enums.ProjectType
import org.wycliffeassociates.otter.jvm.workbookapp.ui.NavigationMediator
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.BookWizardViewModel
import tornadofx.*

// TODO: Replace triple string blocks with messages calls if this gets used
class ProjectTypeSelection : Fragment() {

    private val viewModel: BookWizardViewModel by inject()
    private val navigator: NavigationMediator by inject()

    private val breadCrumb = BreadCrumb().apply {
        titleProperty.bind(
            viewModel.projectTypeProperty.stringBinding {
                it?.let {
                    messages[it.value]
                } ?: """Project Type"""
            }
        )
        iconProperty.set(FontIcon(MaterialDesign.MDI_LINK_OFF))
        onClickAction {
            viewModel.projectTypeProperty.value?.let {
                navigator.back()
            }
        }
    }

    override val root = stackpane {
        alignment = Pos.TOP_LEFT
        vbox {
            addClass("book-wizard__root")
            isFillWidth = false

            vbox {
                label("""
                    Choose Project Type
                """.trimIndent()) {
                    addClass("book-wizard__title")
                }
                hbox {
                    addClass("book-wizard__language-card")
                    label {
                        addClass("book-wizard__language")
                        graphic = FontIcon(Material.HEARING)
                        textProperty().bind(
                            viewModel.translationProperty.stringBinding {
                                it?.sourceLanguage?.name
                            }
                        )
                    }
                    label {
                        addClass("book-wizard__divider")
                        graphic = FontIcon(MaterialDesign.MDI_MENU_RIGHT)
                    }
                    label {
                        addClass("book-wizard__language")
                        graphic = FontIcon(MaterialDesign.MDI_VOICE)
                        textProperty().bind(
                            viewModel.translationProperty.stringBinding {
                                it?.targetLanguage?.name
                            }
                        )
                    }
                }
            }
            region {
                addClass("book-wizard__header-gap")
            }
            hbox {
                addClass("book-wizard__project-type-card", "book-wizard__project-type-card--primary")
                label {
                    addClass("book-wizard__project-type-icon")
                    graphic = FontIcon(MaterialDesign.MDI_LINK_OFF)
                }
                vbox {
                    spacing = 10.0
                    label("""
                        Drafting Project
                    """.trimIndent()) {
                        addClass("book-wizard__project-type-title")
                    }
                    label("""
                        Drafting projects emphasize MAST methodologies. 
                        Recordings are translated from the source language at the chunk level.
                    """.trimIndent()) {
                        addClass("book-wizard__project-type-info")
                        isWrapText = true
                    }
                    button(messages["select"]) {
                        addClass("btn", "btn--cta")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        action {
                            viewModel.projectTypeProperty.set(ProjectType.DRAFTING)
                        }
                    }
                }
            }
            hbox {
                addClass("book-wizard__project-type-card", "book-wizard__project-type-card--secondary")
                label {
                    addClass("book-wizard__project-type-icon")
                    graphic = FontIcon(MaterialDesign.MDI_LINK_OFF)
                }
                vbox {
                    spacing = 10.0
                    label("""
                        Final Recording Project
                    """.trimIndent()) {
                        addClass("book-wizard__project-type-title")
                    }
                    label("""
                        Once a translation has been created, it may be converted 
                        to a final recording project which emphasizes re-recording 
                        the translated material a chapter at a time.
                        """.trimIndent()) {
                        addClass("book-wizard__project-type-info")
                        isWrapText = true
                    }
                    button(messages["select"]) {
                        addClass("btn", "btn--secondary")
                        graphic = FontIcon(MaterialDesign.MDI_ARROW_RIGHT)
                        action {
                            viewModel.projectTypeProperty.set(ProjectType.FINAL_RECORDING)
                        }
                    }
                }
            }
        }
    }

    init {
        importStylesheet(resources.get("/css/book-wizard.css"))
    }

    override fun onDock() {
        navigator.dock(this, breadCrumb)
        viewModel.reset()
    }
}
