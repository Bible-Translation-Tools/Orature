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
package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import javafx.scene.input.KeyCode
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookBannerModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookItemModel
import tornadofx.*

class ChapterCell : ListCell<WorkbookItemModel>() {

    private val chapterCard = ChapterCard()
    private val workbookBanner = WorkbookBanner()

    override fun updateItem(item: WorkbookItemModel?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            graphic = null
            return
        }

        graphic = when (item) {
            is ChapterCardModel -> prepareChapterCard(item)
            is WorkbookBannerModel -> prepareWorkbookBanner(item)
            else -> null
        }
    }

    private fun prepareChapterCard(item: ChapterCardModel): ChapterCard {
        return chapterCard.apply {
            titleProperty.set(item.title)
            notStartedTextProperty.set(FX.messages["notStarted"])

            if (isSelected) {
                listView.setOnKeyPressed {
                    when (it.code) {
                        KeyCode.ENTER, KeyCode.SPACE -> {
                            item.source?.let { chapter ->
                                item.onClick(chapter)
                            }
                        }
                    }
                }
            }

            setOnAction {
                item.source?.let { chapter ->
                    item.onClick(chapter)
                }
            }
        }
    }

    private fun prepareWorkbookBanner(item: WorkbookBannerModel): WorkbookBanner {
        return workbookBanner.apply {
            backgroundArtworkProperty.set(item.coverArt)
            bookTitleProperty.set(item.title)
            resourceTitleProperty.set(item.rcTitle)
            hideDeleteButtonProperty.set(item.rcMetadataProperty.value.type == ContainerType.Help)

            deleteTitleProperty.set(FX.messages["delete"])

            exportTitleProperty.set(
                when (item.rcType) {
                    ContainerType.Book, ContainerType.Bundle -> FX.messages["exportProject"]
                    ContainerType.Help -> FX.messages["exportResource"]
                    else -> ""
                }
            )

            onDeleteAction { item.onDelete() }
            onExportAction { item.onExport() }
        }
    }
}
