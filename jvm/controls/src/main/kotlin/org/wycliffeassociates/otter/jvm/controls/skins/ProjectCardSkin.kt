package org.wycliffeassociates.otter.jvm.controls.skins

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXListView
import com.jfoenix.controls.JFXPopup
import com.jfoenix.controls.JFXRippler
import com.jfoenix.controls.JFXRippler.RipplerMask
import com.jfoenix.controls.JFXRippler.RipplerPos
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.SkinBase
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon
import org.wycliffeassociates.otter.jvm.controls.card.ProjectCard
import tornadofx.onChange


class ProjectCardSkin(private val card: ProjectCard) : SkinBase<ProjectCard>(card) {

    @FXML
    lateinit var bookTitle: Text
    @FXML
    lateinit var cardMoreButton: FontIcon
    @FXML
    lateinit var bookSlug: Text
    @FXML
    lateinit var language: Label
    @FXML
    lateinit var cardPrimaryButton: JFXButton

    val list = JFXListView<Label>()

    init {
        loadFXML()
        initializeControl()
    }

    private fun initializeControl() {
        bindText()
        bindActions()
        bindPopup()
    }

    private fun bindText() {
        bookTitle.textProperty().bind(card.titleTextProperty())
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
        list.items = card.extraActions
        val popup = JFXPopup(list)
        cardMoreButton.setOnMouseClicked {
            popup.show(cardMoreButton, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT)
        }
    }

    private fun loadFXML() {
        val loader = FXMLLoader(javaClass.getResource("ProjectCard.fxml"))
        loader.setController(this)
        val root: Node = loader.load()
        children.add(root)
    }
}