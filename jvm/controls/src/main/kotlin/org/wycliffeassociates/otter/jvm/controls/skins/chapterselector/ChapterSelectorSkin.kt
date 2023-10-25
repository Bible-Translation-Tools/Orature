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
package org.wycliffeassociates.otter.jvm.controls.skins.chapterselector

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import org.wycliffeassociates.otter.jvm.controls.chapterselector.ChapterSelector

class ChapterSelectorSkin(val selector: ChapterSelector) : SkinBase<ChapterSelector>(selector) {
    @FXML
    lateinit var chapterTitle: Label

    @FXML
    lateinit var prevChapterBtn: Button

    @FXML
    lateinit var nextChapterBtn: Button

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        prevChapterBtn.disableProperty().bind(selector.prevDisabledProperty)
        nextChapterBtn.disableProperty().bind(selector.nextDisabledProperty)

        bindText()
        bindAction()
    }

    private fun bindText() {
        chapterTitle.apply {
            textProperty().bind(selector.chapterTitleProperty)
            onMouseClickedProperty().bind(selector.onOpenChapterGridActionProperty)
        }
    }

    private fun bindAction() {
        prevChapterBtn.apply {
            onActionProperty().bind(selector.onPrevChapterActionProperty)
        }
        nextChapterBtn.apply {
            onActionProperty().bind(selector.onNextChapterActionProperty)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ChapterSelector.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
