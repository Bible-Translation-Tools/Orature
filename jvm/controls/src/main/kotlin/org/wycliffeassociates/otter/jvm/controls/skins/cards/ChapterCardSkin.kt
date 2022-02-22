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
package org.wycliffeassociates.otter.jvm.controls.skins.cards

import com.sun.javafx.scene.control.behavior.ButtonBehavior
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.VBox
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ChapterCardSkin(private val card: ChapterCard) : SkinBase<ChapterCard>(card) {

    private val behavior = ButtonBehavior(card)

    @FXML
    lateinit var coverArt: ImageView

    @FXML
    lateinit var title: Label

    @FXML
    lateinit var recordedProgress: ProgressBar

    @FXML
    lateinit var selectedProgress: ProgressBar

    @FXML
    lateinit var recordedChunks: Label

    @FXML
    lateinit var selectedChunks: Label

    @FXML
    lateinit var progressBars: VBox

    @FXML
    lateinit var notStarted: Label

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        card.coverArtProperty.onChangeAndDoNow {
            it?.let {
                coverArt.image = Image(it.inputStream())
            }
        }
        coverArt.apply {
            visibleWhen { card.coverArtProperty.isNotNull }
        }
        title.visibleWhen { card.coverArtProperty.isNull }

        title.apply {
            textProperty().bind(card.titleProperty)
        }

        recordedProgress.apply {
            progressProperty().bind(card.recordedProgressBinding())
        }

        selectedProgress.apply {
            progressProperty().bind(card.selectedProgressBinding())
        }

        recordedChunks.apply {
            textProperty().bind(card.recordedChunksBinding())
        }

        selectedChunks.apply {
            textProperty().bind(card.selectedChunksBinding())
        }

        progressBars.apply {
            isVisible = false
            isManaged = false
        }

        notStarted.apply {
            textProperty().bind(card.notStartedTextProperty)
            hiddenWhen(card.userHasChunkedProperty)
            managedWhen(visibleProperty())
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ChapterCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }

    override fun dispose() {
        super.dispose()
        behavior.dispose()
    }
}
