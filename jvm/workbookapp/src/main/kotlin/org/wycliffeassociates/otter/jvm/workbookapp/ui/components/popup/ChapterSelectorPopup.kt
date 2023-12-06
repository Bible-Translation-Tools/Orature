package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.Node
import javafx.scene.control.PopupControl
import javafx.scene.control.ScrollPane
import javafx.scene.control.Skin
import javafx.scene.layout.VBox
import javafx.stage.Window
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterSelectorPopup : PopupControl() {

    val chapterGridItemList: MutableList<ChapterGridItemData> = mutableListOf()
    private val chapterGrid = ChapterGrid(chapterGridItemList)

    init {
        isAutoHide = true
    }

    override fun show(owner: Window?) {
        super.show(owner)
        chapterGrid.focusOnSelectedChapter()
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChapterSelectorPopupSkin(this, chapterGrid)
    }

    fun updateChapterGrid(newChapterList: List<ChapterGridItemData>) {
        chapterGridItemList.clear()
        chapterGridItemList.addAll(newChapterList)
        chapterGrid.updateChapterGridNodes()
    }

}

class ChapterSelectorPopupSkin(
    val control: ChapterSelectorPopup,
    chapterGrid: ChapterGrid
) : Skin<ChapterSelectorPopup> {

    private val root = VBox().apply {
        addClass("chapter-selector-popup")

        scrollpane {
            addClass("chapter-selector-popup__scroll-pane")
            isFitToWidth = true

            add(chapterGrid)

            runLater { customizeScrollbarSkin() }
        }
    }

    override fun getSkinnable(): ChapterSelectorPopup {
        return control
    }

    override fun getNode(): Node {
        return root
    }

    override fun dispose() {

    }
}
