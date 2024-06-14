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
import org.wycliffeassociates.otter.jvm.utils.onChangeWithDisposer
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
    val chapterGrid: ChapterGrid
) : Skin<ChapterSelectorPopup> {

    private lateinit var scrollPane: ScrollPane

    private val root = VBox().apply {
        addClass("chapter-selector-popup")
        scrollpane {
            scrollPane = this
            addClass("chapter-selector-popup__scroll-pane")
            isFitToWidth = true

            add(chapterGrid)

            runLater {
                customizeScrollbarSkin()
            }
        }
    }

    init {
        control.showingProperty().onChangeWithDisposer {
            if (it == true) {
                scrollToSelected()
            }
        }
    }

    private fun scrollToSelected() {
        val selectedNode = chapterGrid.getSelectedChapter()
        selectedNode?.let {
            val contentBounds = scrollPane.content.layoutBounds
            val nodeBounds = selectedNode.boundsInParent
            scrollPane.vvalue = nodeBounds.minY / contentBounds.height
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
