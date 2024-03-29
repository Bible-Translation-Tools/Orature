/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.screens.dialogs

import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.material.Material
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.wycliffeassociates.otter.common.data.primitives.Contributor
import org.wycliffeassociates.otter.jvm.controls.dialog.OtterDialog
import org.wycliffeassociates.otter.jvm.controls.ContributorInfo
import org.wycliffeassociates.otter.jvm.controls.model.ContributorCellData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.ExportChapterViewModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.SettingsViewModel
import tornadofx.*

class ExportChapterDialog : OtterDialog() {
    val contributors = observableListOf<Contributor>()
    private val viewModel: ExportChapterViewModel by inject()

    private val settingsViewModel: SettingsViewModel by inject()

    private val content = VBox().apply {
        addClass("contributor-dialog")

        vbox {
            hbox {
                addClass("contributor-dialog__header")
                label (messages["exportChapter"]) {
                    addClass("h3")
                }
                region { hgrow = Priority.ALWAYS }
                button {
                    addClass("btn", "btn--secondary")
                    graphic = FontIcon(MaterialDesign.MDI_CLOSE)
                    tooltip(messages["close"])
                    action { close() }
                }
            }
            add(createContributorSection(contributors))
        }
        hbox {
            hgrow = Priority.ALWAYS
            addClass("contributor-dialog__action")

            button (messages["exportChapter"]) {
                addClass("btn", "btn--primary", "btn--borderless", "contributor-dialog__export-btn")
                graphic = FontIcon(Material.UPLOAD_FILE)
                hgrow = Priority.ALWAYS
                tooltip(this.text)

                action {
                    export()
                    close()
                }
            }
            button (messages["cancel"]) {
                addClass("btn", "btn--secondary")
                graphic = FontIcon("gmi-close")
                hgrow = Priority.SOMETIMES
                tooltip(this.text)

                action {
                    close()
                }
            }
        }

        textflow {
            text(messages["exportLicenseDescription"]) {
                addClass("contributor__section-text")
            }
            hyperlink(messages["licenseCCBYSA"]) {
                addClass("wa-text--hyperlink", "contributor__license-link")
                val url = "https://creativecommons.org/licenses/by-sa/4.0/"
                tooltip(url)
                action {
                    FX.application.hostServices.showDocument(url)
                }
            }
        }
    }

    init {
        setContent(content)
    }

    private fun createContributorSection(contributors: ObservableList<Contributor>): ContributorInfo {
        return ContributorInfo(contributors)
            .apply {
                addContributorCallbackProperty.set(
                    EventHandler {
                        viewModel.addContributor(it.source as String)
                    }
                )
                removeContributorCallbackProperty.set(
                    EventHandler {
                        val indexToRemove = it.source as Int
                        viewModel.removeContributor(indexToRemove)
                    }
                )
                editContributorCallbackProperty.set(
                    EventHandler {
                        val data = it.source as ContributorCellData
                        viewModel.editContributor(data)
                        lastModifiedIndex.set(data.index)
                    }
                )
            }
    }

    fun export() {
        val directory = chooseDirectory(FX.messages["exportChapter"])
        directory?.let {
            viewModel.export(it)
        }
    }

    override fun onDock() {
        super.onDock()
        themeProperty.set(settingsViewModel.appColorMode.value)
    }
}
