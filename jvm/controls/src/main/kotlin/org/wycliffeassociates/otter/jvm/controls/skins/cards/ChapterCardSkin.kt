package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.wycliffeassociates.otter.jvm.controls.card.ChapterCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ChapterCardSkin(private val card: ChapterCard): SkinBase<ChapterCard>(card) {

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

    init {
        loadFXML()
        initializeControl()

        importStylesheet(javaClass.getResource("/css/chapter-card.css").toExternalForm())
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
            textProperty().bind(card.titleBinding())
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
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ChapterCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
