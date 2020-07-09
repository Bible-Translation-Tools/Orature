package org.wycliffeassociates.otter.jvm.controls.skins.cards

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXPopup
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.card.ProjectCard
import org.wycliffeassociates.otter.jvm.utils.onChangeAndDoNow
import tornadofx.*

class ProjectCardSkin(private val card: ProjectCard) : SkinBase<ProjectCard>(card) {

    @FXML
    lateinit var root: VBox
    @FXML
    lateinit var bookTitle: Label
    @FXML
    lateinit var cardMoreButton: JFXButton
    @FXML
    lateinit var bookSlug: Text
    @FXML
    lateinit var language: Label
    @FXML
    lateinit var cardPrimaryButton: JFXButton
    @FXML
    lateinit var coverArt: ImageView

    private val popup = JFXPopup()
    private val list = JFXListView<Label>().apply {
        addClass("project-card__popup-list")
    }

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bindText()
        bindActions()
        bindPopup()

        coverArt.fitWidthProperty().bind(root.widthProperty())

        card.coverArtProperty().onChangeAndDoNow {
            it?.let {
                coverArt.image = Image(it.inputStream())
            }
        }
        coverArt.visibleWhen { card.coverArtProperty().isNotNull }
        bookSlug.visibleWhen { card.coverArtProperty().isNull }
        language.visibleWhen { card.coverArtProperty().isNull }
    }

    private fun bindText() {
        bookTitle.textProperty().bind(card.titleTextProperty())
        bookTitle.tooltip {
            textProperty().bind(card.titleTextProperty())
        }
        bookSlug.textProperty().bind(card.slugTextProperty())
        language.textProperty().bind(card.languageTextProperty())
        cardPrimaryButton.textProperty().bind(card.actionTextProperty())
    }

    private fun bindActions() {
        cardPrimaryButton.setOnAction {
            card.onPrimaryActionProperty().value.invoke()
        }
        card.onPrimaryActionProperty().onChange { op ->
            cardPrimaryButton.setOnAction {
                op?.invoke()
            }
        }
    }

    private fun bindPopup() {
        card.secondaryActionsList.onChangeAndDoNow { actions ->
            val popup = JFXPopup()
            val items = actions.map { action ->
                Label().apply {
                    vgrow = Priority.ALWAYS
                    text = action.text
                    graphic = FontIcon(action.iconCode)
                    setOnMouseClicked {
                        action.onClicked.invoke()
                    }
                }
            }
            list.setOnMouseClicked {
                popup.hide()
            }
            list.items.setAll(items)
            popup.popupContent = list
            cardMoreButton.setOnAction {
                popup.show(cardMoreButton)
            }
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ProjectCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}