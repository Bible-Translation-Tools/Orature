package org.wycliffeassociates.otter.jvm.workbookapp.ui.components

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.common.data.primitives.ContainerType
import org.wycliffeassociates.otter.common.data.primitives.ResourceMetadata
import org.wycliffeassociates.otter.jvm.controls.banner.WorkbookBanner
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.workbookapp.ui.model.CardData
import org.wycliffeassociates.otter.jvm.workbookapp.ui.viewmodel.WorkbookPageViewModel
import tornadofx.*

class ChapterView(metadata: ResourceMetadata, onDelete: () -> Unit): VBox() {
    private val viewModel: WorkbookPageViewModel = find()
    val cardDataProperty = SimpleObjectProperty<CardData>(null)
    val cardData: CardData? by cardDataProperty

    private val chapterCard = ChapterCard().apply {
        addClass("workbook-page__chapter-card")

        cardDataProperty.onChange {
            titleProperty.set(it?.sort.toString())

            setOnMouseClicked {
                cardData?.chapterSource?.let { chapter ->
                    viewModel.selectedChapterIndexProperty.set(
                        viewModel.chapters.indexOf(cardData)
                    )
                    viewModel.navigate(chapter)
                }
            }
        }

        visibleWhen(cardDataProperty.isNotNull)
        managedWhen(visibleProperty())
    }

    private val bookBanner = WorkbookBanner().apply {
        addClass("workbook-page__workbook-banner")

        val workbook = viewModel.workbookDataStore.workbook

        backgroundImageFileProperty.set(workbook.coverArtAccessor.getArtwork())
        bookTitleProperty.set(workbook.target.title)
        resourceTitleProperty.set(metadata.title)

        deleteTitleProperty.set(FX.messages["delete"])

        exportTitleProperty.set(
            when (metadata.type) {
                ContainerType.Book, ContainerType.Bundle -> FX.messages["exportProject"]
                ContainerType.Help -> FX.messages["exportResource"]
                else -> ""
            }
        )

        onDeleteAction {
            onDelete()
        }

        onExportAction {
            val directory = chooseDirectory(FX.messages["exportProject"])
            directory?.let {
                viewModel.exportWorkbook(it)
            }
        }

        hiddenWhen(cardDataProperty.isNotNull)
        managedWhen(visibleProperty())
    }

    init {
        add(bookBanner)
        add(chapterCard)
    }
}
