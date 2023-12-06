package org.wycliffeassociates.otter.jvm.workbookapp.ui.components.popup

import javafx.scene.Node
import javafx.scene.control.PopupControl
import javafx.scene.control.ScrollPane
import javafx.scene.control.Skin
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterGrid
import org.wycliffeassociates.otter.jvm.controls.customizeScrollbarSkin
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

class ChapterGridMenu : PopupControl() {

    val chapterGridItemList: MutableList<ChapterGridItemData> = mutableListOf()
    private val chapterGrid = ChapterGrid(chapterGridItemList)

    init {
        isAutoHide = true
    }

    override fun createDefaultSkin(): Skin<*> {
        return ChapterMenuSkin(this, chapterGrid)
    }

    fun updateChapterGrid(newChapterList: List<ChapterGridItemData>) {
        chapterGridItemList.clear()
        chapterGridItemList.addAll(newChapterList)
        chapterGrid.updateChapterGridNodes()
    }

}

class ChapterMenuSkin(
    val control: ChapterGridMenu,
    chapterGrid: ChapterGrid
) : Skin<ChapterGridMenu> {

    private val root = VBox().apply {
        addClass("chapter-grid-context-menu")

        add(
            ScrollPane(chapterGrid).apply {
                addClass("chapter-grid-context-menu__scroll-pane")
                isFitToWidth = true

                runLater { customizeScrollbarSkin() }
            }
        )
    }

    override fun getSkinnable(): ChapterGridMenu {
        return control
    }

    override fun getNode(): Node {
        return root
    }

    override fun dispose() {

    }
}
