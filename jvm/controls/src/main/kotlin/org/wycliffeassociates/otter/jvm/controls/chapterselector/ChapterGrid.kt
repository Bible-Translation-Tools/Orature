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
package org.wycliffeassociates.otter.jvm.controls.chapterselector

import javafx.scene.layout.GridPane
import javafx.scene.layout.StackPane
import org.kordamp.ikonli.javafx.FontIcon
import org.kordamp.ikonli.materialdesign.MaterialDesign
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.jvm.controls.event.NavigateChapterEvent
import org.wycliffeassociates.otter.jvm.controls.model.ChapterGridItemData
import tornadofx.*

private const val GRID_COLUMNS = 5

class ChapterGrid(val list: List<ChapterGridItemData>) : GridPane() {
    private val logger = LoggerFactory.getLogger(ChapterGrid::class.java)

    init {
        addClass("chapter-grid")
        addChaptersToGrid()
    }

    private fun selectChapter(chapterIndex: Int) {
        logger.info("Selecting chapter ${chapterIndex}")
        FX.eventbus.fire(NavigateChapterEvent(chapterIndex))
    }

    fun focusOnSelectedChapter() {
        lookupAll(":selected").firstOrNull()?.requestFocus()
    }

    fun updateChapterGridNodes() {
        children.clear()
        columnConstraints.clear()
        addChaptersToGrid()
    }

    private fun addChaptersToGrid() {
        list.forEachIndexed { index, chapter ->
            val node = StackPane().apply {
                button(chapter.number.toString()) {
                    addClass(
                        "btn", "btn--tertiary", "btn--borderless", "chapter-grid__btn"
                    )
                    togglePseudoClass("selected", chapter.selected)
                    setOnAction {
                        selectChapter(chapter.number)
                    }
                }
                hbox {
                    addClass("chapter-grid__icon-alignment-box")
                    add(
                        FontIcon(MaterialDesign.MDI_CHECK_CIRCLE).apply { addClass("complete-icon") }
                    )
                    isMouseTransparent = true
                    isPickOnBounds = false
                    visibleWhen { chapter.completedProperty }
                    managedWhen { visibleProperty() }
                }
            }
            this.add(node, index % GRID_COLUMNS, index / GRID_COLUMNS)
        }
    }
}