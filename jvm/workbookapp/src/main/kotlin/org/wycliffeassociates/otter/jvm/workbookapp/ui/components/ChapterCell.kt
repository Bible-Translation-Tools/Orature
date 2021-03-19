package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.scene.control.ListCell
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.ChapterCardModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookBannerModel
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.WorkbookItemModel
import tornadofx.*

class ChapterCell: ListCell<WorkbookItemModel>() {

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

            setOnMouseClicked {
                item.source?.let { chapter ->
                    item.onClick(chapter)
                }
            }
        }
    }

    private fun prepareWorkbookBanner(item: WorkbookBannerModel): WorkbookBanner {
        return workbookBanner.apply {
            backgroundImageFileProperty.set(item.coverArt)
            bookTitleProperty.set(item.title)
            resourceTitleProperty.set(item.rcTitle)

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
