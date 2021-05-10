package org.wycliffeassociates.otter.jvm.controls.skins.cards

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundImage
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.shape.Rectangle
import org.wycliffeassociates.otter.jvm.controls.card.BookCard
import tornadofx.*
import java.io.File

class BookCardSkin(private val card: BookCard) : SkinBase<BookCard>(card) {

    @FXML
    lateinit var bookCardPlaceholder: HBox

    @FXML
    lateinit var coverArt: Region

    @FXML
    lateinit var bookCardInfo: VBox

    @FXML
    lateinit var title: Label

    @FXML
    lateinit var projectType: Label

    @FXML
    lateinit var addBookBtn: Button

    private val graphicRadius = 15.0

    init {
        loadFXML()
        initializeControl()

        importStylesheet(javaClass.getResource("/css/book-card.css").toExternalForm())
    }

    private fun initializeControl() {
        bookCardPlaceholder.apply {
            visibleProperty().bind(
                card.coverArtProperty.isNull.or(card.newBookProperty)
            )
        }
        coverArt.apply {
            backgroundProperty().bind(
                card.coverArtProperty.objectBinding {
                    it?.let { Background(backgroundImage(it)) }
                }
            )
            val rect = Rectangle().apply {
                widthProperty().bind(coverArt.widthProperty())
                heightProperty().bind(coverArt.heightProperty())
                arcWidth = graphicRadius
                arcHeight = graphicRadius
            }
            clip = rect
            visibleProperty().bind(
                card.coverArtProperty.isNotNull.and(card.newBookProperty.not())
            )
        }
        bookCardInfo.apply {
            visibleProperty().bind(card.newBookProperty.not())
        }
        title.apply {
            textProperty().bind(card.titleProperty)
        }
        projectType.apply {
            textProperty().bind(card.projectTypeProperty)
        }
        addBookBtn.apply {
            textProperty().bind(card.addBookTextProperty)
            visibleProperty().bind(card.newBookProperty)
            onActionProperty().bind(card.onAddBookActionProperty)
        }
    }

    private fun backgroundImage(file: File): BackgroundImage {
        val image = Image(file.inputStream())
        val backgroundSize = BackgroundSize(
            BackgroundSize.AUTO,
            BackgroundSize.AUTO,
            true,
            true,
            false,
            true
        )
        return BackgroundImage(
            image,
            BackgroundRepeat.NO_REPEAT,
            BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            backgroundSize
        )
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("BookCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}
